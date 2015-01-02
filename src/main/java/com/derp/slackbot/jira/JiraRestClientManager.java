package com.derp.slackbot.jira;

import java.net.URI;
import java.net.URISyntaxException;

import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.internal.jersey.JerseyJiraRestClientFactory;

/**
 * Class for working with JIRA rest client.
 */
public class JiraRestClientManager {

	private String jiraUrl;
	private String username;
	private String password;

	public JiraRestClientManager(String jiraUrl, String username, String password) {
		this.jiraUrl = jiraUrl;
		this.username = username;
		this.password = password;
	}

	/**
	 * Get an {@link Issue} by key.
	 *
	 * @param key the key for the issue
	 * @return an issue, or null if it does not exist
	 */
	public Issue getIssueByKey(String key) {
		JiraRestClient client = getJiraClient();
		final NullProgressMonitor pm = new NullProgressMonitor();
		final Issue issue = client.getIssueClient().getIssue(key, pm);

		return issue;
	}

	/**
	 * Get a {@link JiraRestClient}.
	 *
	 * @return a JiraRestClient
	 */
	private JiraRestClient getJiraClient() {
		final JerseyJiraRestClientFactory factory = new JerseyJiraRestClientFactory();
		final URI jiraServerUri;
		try {
			jiraServerUri = new URI(jiraUrl);
		} catch (URISyntaxException e) {
			System.out.println("Unable to create jira client."); // TODO:
			return null;
		}

		final JiraRestClient restClient = factory.createWithBasicHttpAuthentication(jiraServerUri, username, password);
		return restClient;
	}

	public void setJiraUrl(String jiraUrl) {
		this.jiraUrl = jiraUrl;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
