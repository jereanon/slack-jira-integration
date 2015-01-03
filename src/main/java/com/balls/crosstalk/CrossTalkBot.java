package com.balls.crosstalk;

import com.balls.slack.SlackRealTimeMessagingConnection;
import com.balls.slack.messages.SlackMessage;
import com.balls.slack.messages.SlackMessageHandler;
import com.balls.slack.messages.SlackMessagePayload;

/**
 * The bot that relays messages between Slack companies
 */
public class CrossTalkBot implements SlackMessageHandler {
    private final SlackRealTimeMessagingConnection slackConnection;

    public CrossTalkBot(SlackRealTimeMessagingConnection connection) {
        this.slackConnection = connection;
    }

    @Override
    public void onSlackMessage(SlackMessagePayload slackMessagePayload) {
        // exit early, we're only interested in messages
        if (!(slackMessagePayload instanceof SlackMessage)) {
            return;
        }

        SlackMessage slackMessage = (SlackMessage) slackMessagePayload;

        // slackConnection.sendMessageToChannel(slackMessage.getChannel(), "i herd");
    }
}
