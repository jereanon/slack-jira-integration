package com.balls.crosstalk;

import com.balls.slack.SlackRestClient;
import com.balls.slack.messages.SlackMessage;
import com.balls.slack.messages.SlackMessageHandler;
import com.balls.slack.messages.SlackMessagePayload;

/**
 * The bot that relays messages between Slack teams
 */
public class CrossTalkBot implements SlackMessageHandler {
    private final SlackRestClient slackRestClient;

    public CrossTalkBot(String restUrl) {
        this.slackRestClient = new SlackRestClient(restUrl);
        slackRestClient.sendMessage("I R ALIVE");
    }

    @Override
    public void onSlackMessage(SlackMessagePayload slackMessagePayload) {
        // exit early, we're only interested in messages
        if (!(slackMessagePayload instanceof SlackMessage)) {
            return;
        }

        SlackMessage slackMessage = (SlackMessage) slackMessagePayload;
        slackRestClient.sendMessage(slackMessage.getChannel(), slackMessage.getUser(), "i herd");
    }
}
