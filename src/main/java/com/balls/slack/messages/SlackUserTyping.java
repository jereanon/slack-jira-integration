package com.balls.slack.messages;

/**
 * Users be typin'
 */
public class SlackUserTyping extends SlackMessagePayload {

	private String channel;
	private String user;

	@Override
	public String getType() {
		return "user_typing";
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

}
