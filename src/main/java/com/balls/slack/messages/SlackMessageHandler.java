package com.balls.slack.messages;

/**
 * Interface that defines behaviors for handling slack messages.
 */
public interface SlackMessageHandler {

	/**
	 * When a message is received from slack.
	 *
	 * @param slackMessagePayload the message payload
	 */
	void onSlackMessage(SlackMessagePayload slackMessagePayload);
}
