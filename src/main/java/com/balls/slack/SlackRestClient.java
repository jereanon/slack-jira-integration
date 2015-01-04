package com.balls.slack;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Client for accessing the Slack REST API
 */
public class SlackRestClient {
    private final String restUrl;

    public SlackRestClient(String restUrl) {
        this.restUrl = restUrl;
    }

    /**
     * Send a message to a Slack channel with a user name
     * @param channel     the channel name
     * @param asUser      the user name
     * @param messageText the message
     */
    public void sendMessage(String channel, String asUser, String messageText) {
        Map<String, Object> payload = new HashMap<String, Object>();
        payload.put("text", messageText);
        // TODO: gotta look up username and channel etc through API because we only get their IDs back from rtm
//            payload.put("username", asUser);
//            payload.put("channel", channel);

        sendPayload(payload);
    }

    /**
     * Send a message to the default Slack channel using the default user name
     * @param messageText the message
     */
    public void sendMessage(String messageText) {
        Map<String, Object> payload = new HashMap<String, Object>();
        payload.put("text", messageText);

        sendPayload(payload);
    }

    private JsonNode sendPayload(Map<String, Object> payload) {
        // Build dat JSON payload
        URI uri = null;
        HttpEntity payloadEntity = null;
        try {
            uri = new URI(this.restUrl);
            payloadEntity = new StringEntity(SlackRequestUtil.mapToJsonString(payload));
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return null;
        }

        // Send dat data!
        HttpPost post = new HttpPost(uri);
        post.addHeader("Content-Type", "application/json");
        post.setEntity(payloadEntity);
        JsonNode node = SlackRequestUtil.executeRequestToJsonNode(post);

        System.out.println(node);

        return node;
    }
}
