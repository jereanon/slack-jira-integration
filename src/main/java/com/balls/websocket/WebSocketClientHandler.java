package com.balls.websocket;

import org.java_websocket.handshake.ServerHandshake;

/**
 * Interface that defines methods to be handled by websocket clients.
 */
public interface WebSocketClientHandler {
	
	void onOpen(ServerHandshake serverHandshake);

	void onMessage(String s);
	
	void onClose(int i, String s, boolean b);

	void onError(Exception e);
}
