package com.balls.jira;

import org.kohsuke.args4j.Option;

/**
 * The command line options.
 */
public class CommandLineOptions {

	private String jiraUrl;
	private String jiraUsername;
	private String jiraPassword;

	private String slackUrl;
	private String slackApiKey;

	public String getJiraUrl() {
		return jiraUrl;
	}

	@Option(name="-jiraUrl",usage="url to connect to jira", required = true)
	public void setJiraUrl(String jiraUrl) {
		this.jiraUrl = jiraUrl;
	}

	public String getJiraUsername() {
		return jiraUsername;
	}

	@Option(name="-jiraUsername",usage="Sets a jira username", required = true)
	public void setJiraUsername(String jiraUsername) {
		this.jiraUsername = jiraUsername;
	}

	public String getJiraPassword() {
		return jiraPassword;
	}

	@Option(name="-jiraPassword",usage="Sets a jira password", required = true)
	public void setJiraPassword(String jiraPassword) {
		this.jiraPassword = jiraPassword;
	}

	public String getSlackUrl() {
		return slackUrl;
	}

	@Option(name="-slackUrl",usage="Sets a slack url", required = true)
	public void setSlackUrl(String slackUrl) {
		this.slackUrl = slackUrl;
	}

	public String getSlackApiKey() {
		return slackApiKey;
	}

	@Option(name="-slackApiKey",usage="Sets a slack api key", required = true)
	public void setSlackApiKey(String slackApiKey) {
		this.slackApiKey = slackApiKey;
	}
}
