package com.derp.slackbot;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.atlassian.jira.rest.client.domain.Issue;
import com.derp.slackbot.jira.JiraRestClientManager;
import com.derp.slackbot.slack.SlackRealTimeMessagingConnection;
import com.derp.slackbot.slack.messages.SlackMessage;
import com.derp.slackbot.slack.messages.SlackMessageHandler;
import com.derp.slackbot.slack.messages.SlackMessagePayload;

/**
 * A bot that works with Jira.
 */
public class JiraIntgrationBot implements SlackMessageHandler {

	private final SlackRealTimeMessagingConnection slackConnection;
	private JiraRestClientManager jiraRestClientManager;

	private static final Pattern JIRA_KEY_PATTERN = Pattern.compile("[A-Z]-[0-9]");

	public JiraIntgrationBot(JiraRestClientManager jiraRestClientManager, SlackRealTimeMessagingConnection slackConnection) {
		this.jiraRestClientManager = jiraRestClientManager;
		this.slackConnection = slackConnection;
	}

	@Override
	public void onSlackMessage(SlackMessagePayload slackMessagePayload) {

		// exit early, we're only interested in messages
		if (!(slackMessagePayload instanceof SlackMessage)) {
			return;
		}

		SlackMessage slackMessage = (SlackMessage)slackMessagePayload;
		boolean contains = JIRA_KEY_PATTERN.matcher(slackMessage.getText()).find();

		// find all the issues, if the message contains any issues
		List<String> issues = new ArrayList<String>();
		if (contains) {
			String[] pieces = slackMessage.getText().split(" ");
			for (String piece : pieces) {
				if (JIRA_KEY_PATTERN.matcher(piece).find()) {
					issues.add(piece);
				}
			}
		}

		// message for each issue
		for (String issueKey : issues) {
			Issue issue = jiraRestClientManager.getIssueByKey(issueKey);
			if (issue==null) {
				System.out.println("issue: "+issueKey+" not found.");
				continue;
			}

			String messageText = issueKey + " " + issue.getDescription();
			slackConnection.sendMessageToChannel(slackMessage.getChannel(), messageText);
		}

	}
}
