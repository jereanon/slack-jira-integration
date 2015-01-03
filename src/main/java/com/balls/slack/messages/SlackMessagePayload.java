package com.balls.slack.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * A message received from a Slack server.
 */
@JsonTypeInfo(
		use = JsonTypeInfo.Id.NAME,
		include = JsonTypeInfo.As.PROPERTY,
		property = "type")
@JsonSubTypes({
		@JsonSubTypes.Type(value = SlackMessage.class, name = "message")
})
public abstract class SlackMessagePayload {

	@JsonProperty("type")
	private String type;

	@JsonProperty("type")
	public abstract String getType();
}
