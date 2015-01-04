package com.balls.jira;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import com.balls.slack.SlackRealTimeMessagingConnection;

/**
 * Run the application.
 */
public class Main {

	public static void main(String[] args) {

		// parse command line options
		CommandLineOptions options = new CommandLineOptions();
		CmdLineParser commandLineParser = new CmdLineParser(options);

		try {
			commandLineParser.parseArgument(args);
		} catch (CmdLineException e) {
			// handling of wrong arguments
			System.err.println(e.getMessage());
			commandLineParser.printUsage(System.err);
			System.exit(0);
		}

		// set all the options
		SlackRealTimeMessagingConnection connection = new SlackRealTimeMessagingConnection(options.getSlackUrl(), "jira-integration", options.getSlackApiKey());
		connection.startRealTimeClient();

		// setup the jira
		JiraRestClientManager jiraRestClientManager = new JiraRestClientManager(options.getJiraUrl(), options.getJiraUsername(), options.getJiraPassword());

		// the bot
		JiraIntegrationBot bot = new JiraIntegrationBot(jiraRestClientManager, connection);
		connection.registerSlackMessageHandler(bot);
	}
}
