package com.balls.jira;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.DateTime;

import com.atlassian.jira.rest.client.domain.Issue;
import com.balls.slack.SlackRealTimeMessagingConnection;
import com.balls.slack.messages.SlackMessage;
import com.balls.slack.messages.SlackMessageHandler;
import com.balls.slack.messages.SlackMessagePayload;

/**
 * A bot that works with Jira.
 */
public class JiraIntegrationBot implements SlackMessageHandler {

	private final SlackRealTimeMessagingConnection slackConnection;
	private JiraRestClientManager jiraRestClientManager;

	private Map<String, List<Pair<Issue, DateTime>>> cacheMap = new HashMap<>();
	private int expirationMillis = 1000 * 60 * 5; // 5 minutes

	private static final Pattern JIRA_KEY_PATTERN = Pattern.compile("[A-Za-z]-[0-9]");

	public JiraIntegrationBot(JiraRestClientManager jiraRestClientManager,
			SlackRealTimeMessagingConnection slackConnection) {
		this.jiraRestClientManager = jiraRestClientManager;
		this.slackConnection = slackConnection;
	}

	/**
	 * When we receive a message, query JIRA for the issue and return a response.
	 *
	 * @param slackMessagePayload the message payload
	 */
	@Override
	public void onSlackMessage(SlackMessagePayload slackMessagePayload) {

		// exit early, we're only interested in messages
		if (!(slackMessagePayload instanceof SlackMessage)) {
			return;
		}

		SlackMessage slackMessage = (SlackMessage)slackMessagePayload;
		boolean contains = JIRA_KEY_PATTERN.matcher(slackMessage.getText()).find();

		// find all the issues, if the message contains any issues
		List<String> issues = new ArrayList<>();
		if (contains) {
			String[] pieces = slackMessage.getText().split(" ");
			for (String piece : pieces) {
				if (JIRA_KEY_PATTERN.matcher(piece).find()) {
					issues.add(piece);
				}
			}
		}

		// message for each issue
		DateTime now = new DateTime();
		for (String issueKey : issues) {
			Issue issue = null;
			try {
				issue = jiraRestClientManager.getIssueByKey(issueKey);
			} catch (Exception ex) {
				System.out.println("Exception getting issue: "+ex.getMessage());
			}
			if (issue==null) {
				System.out.println("issue: "+issueKey+" not found.");
				continue;
			}

			// only notify if we haven't notified within the cacheTime
			if (!canNotify(issue, slackMessage.getChannel())) {
				System.out.println("Skipping issue with key: " + issueKey
						+ " because it is too recent in: " + slackMessage.getChannel());
				continue;
			}

			// create the message text
			String linkText = this.jiraRestClientManager.getJiraUrl()+"/browse/"+issue.getKey();
			String messageText = "issue key: "+ issueKey
					+ " Summary: " + issue.getSummary()
					+ " " + linkText;

			// create the message
			SlackMessage message = new SlackMessage();
			message.setUsername("JIRA_TEST");
			message.setText(messageText);
			message.setChannel(slackMessage.getChannel());

			// fire the message
			slackConnection.sendMessage(message);
		}

	}

	/**
	 * Can we notify about an issue? Add issue to cache.
	 *
	 * @param issue the issue to notify about
	 * @param channel the channel to notify
	 * @return can we notify or not?
	 */
	private boolean canNotify(Issue issue, String channel) {
		DateTime now = new DateTime();

		if (cacheMap.containsKey(channel.toLowerCase())) {
			// check for the pair existing in the cache
			List<Pair<Issue, DateTime>> issueKeys = cacheMap.get(channel.toLowerCase());
			Optional<Pair<Issue, DateTime>> issueExists = issueKeys.stream()
					.filter(p -> p.getLeft().equals(issue))
					.findAny();

			if (issueExists.isPresent()) {
				if (issueExists.get().getRight().isBefore(now.minusMillis(expirationMillis))) {
					// show the issue, add into cache
					issueKeys.add(Pair.of(issue, now));
					return true;
				} else {
					// cache hasn't expired yet
					return false;
				}
			} else {
				// put it in the cache
				issueKeys.add(Pair.of(issue, now));
				return true;
			}
		} else {
			// put it in the cache
			cacheMap.put(channel.toLowerCase(), new ArrayList<Pair<Issue, DateTime>>(){{
				add(Pair.of(issue, now));
			}});
			return true;
		}
	}
}
