package com.balls.slack;

import com.balls.slack.messages.SlackMessageHandler;
import com.balls.slack.messages.SlackMessagePayload;
import com.balls.websocket.WebSocketClientHandler;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.java_websocket.client.DefaultSSLWebSocketClientFactory;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;

/**
 * Connection for Slack.
 */
public class SlackRealTimeMessagingConnection implements WebSocketClientHandler {

	private String connectionUrl;
	private String username;
	private String apiKey;

	private ObjectMapper objectMapper = new ObjectMapper(new JsonFactory());
	private WebSocketClient webSocketClient;

	private Set<SlackMessageHandler> slackMessageHandlers = new HashSet<SlackMessageHandler>();

	public SlackRealTimeMessagingConnection(String connectionUrl, String username, String apiKey) {
		this.connectionUrl = connectionUrl;
		this.username = username;
		this.apiKey = apiKey;
	}

	public void registerSlackMessageHandler(SlackMessageHandler handler) {
		slackMessageHandlers.add(handler);
	}

	/**
	 * Start the real time client.
	 */
	public void startRealTimeClient() {

		URI uri = null;
		try {
			uri = new URIBuilder(connectionUrl+"/api/rtm.start")
					.setParameter("token", apiKey)
					.build();
		} catch (URISyntaxException e) {
			System.out.println(e); // TODO: fix
		}

		HttpGet get = new HttpGet(uri);
		JsonNode node = SlackRequestUtil.executeRequestToJsonNode(get);
		String webSocketUrl = node.get("url").textValue();

		// create teh websocket
		webSocketClient = createJavaWebSocketContainer(webSocketUrl);
	}

	@Override
	public void onOpen(ServerHandshake serverHandshake) {
		System.out.println("OPENED");
	}

	@Override
	public void onMessage(String s) {
		try {
			SlackMessagePayload payload = objectMapper.readValue(s, SlackMessagePayload.class);
			for (SlackMessageHandler handler : slackMessageHandlers) {
				handler.onSlackMessage(payload);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("received message "+s);
	}

	@Override
	public void onClose(int i, String s, boolean b) {
		System.out.println("closed: "+s);
	}

	@Override
	public void onError(Exception e) {
		System.out.println("on error: "+e.getStackTrace());
	}

	/**
	 * A WebSocketClient that uses the default java keystore for SSL.
	 */
	private class SlackRealTimeWebSocketClient extends WebSocketClient {

		private Set<WebSocketClientHandler> handlerSet = new HashSet<WebSocketClientHandler>();

		public SlackRealTimeWebSocketClient(URI uri) {
			super(uri);
		}

		public void registerHandler(WebSocketClientHandler handler) {
			handlerSet.add(handler);
		}

		@Override
		public void onOpen(ServerHandshake serverHandshake) {
			for (WebSocketClientHandler handler : handlerSet) {
				handler.onOpen(serverHandshake);
			}
		}

		@Override
		public void onMessage(String s) {
			for (WebSocketClientHandler handler : handlerSet) {
				handler.onMessage(s);
			}
		}

		@Override
		public void onClose(int i, String s, boolean b) {
			for (WebSocketClientHandler handler : handlerSet) {
				handler.onClose(i, s, b);
			}
		}

		@Override
		public void onError(Exception e) {
			for (WebSocketClientHandler handler : handlerSet) {
				handler.onError(e);
			}
		}
	}

	private WebSocketClient createJavaWebSocketContainer(String endpoint) {

		URI uri;
		try {
			uri = new URI(endpoint);
		} catch (URISyntaxException e) {
			System.out.println("unable to create uri "+e.getStackTrace());
			return null;
		}

		SlackRealTimeWebSocketClient wsClient = new SlackRealTimeWebSocketClient(uri);
		wsClient.registerHandler(this);

		SSLContext sslContext = null;
		try {
			sslContext = SSLContext.getInstance("TLS");
			sslContext.init( null, null, null );
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		}

		wsClient.setWebSocketFactory( new DefaultSSLWebSocketClientFactory( sslContext ) );
		wsClient.connect();
		return wsClient;
	}

	public void setConnectionUrl(String connectionUrl) {
		this.connectionUrl = connectionUrl;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public void setObjectMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

}
