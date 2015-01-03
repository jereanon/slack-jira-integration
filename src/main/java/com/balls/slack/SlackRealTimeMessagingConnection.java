package com.balls.slack;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;

import javax.net.ssl.SSLContext;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.java_websocket.client.DefaultSSLWebSocketClientFactory;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import com.balls.slack.messages.SlackMessage;
import com.balls.websocket.WebSocketClientHandler;
import com.balls.slack.messages.SlackMessageHandler;
import com.balls.slack.messages.SlackMessagePayload;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Connection for Slack.
 */
public class SlackRealTimeMessagingConnection implements WebSocketClientHandler {

	private String connectionUrl;
	private String username;
	private String apiKey;

	private static final Charset EXPECTED_CHARSET = Charset.forName("UTF-8");

	private CloseableHttpClient httpClient = HttpClients.createDefault();
	private ObjectMapper objectMapper = new ObjectMapper(new JsonFactory());
	private WebSocketClient webSocketClient;

	private Set<SlackMessageHandler> slackMessageHandlers = new HashSet<SlackMessageHandler>();
	private boolean isConnected = false;

	public SlackRealTimeMessagingConnection(String connectionUrl, String username, String apiKey) {
		this.connectionUrl = connectionUrl;
		this.username = username;
		this.apiKey = apiKey;
	}

	/**
	 * Register a {@link SlackMessageHandler} to receive messages.
	 *
	 * @param handler a handler to receive messages
	 */
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
		JsonNode node = executeRequestToJsonNode(get);
		String webSocketUrl = node.get("url").textValue();

		// create teh websocket
		webSocketClient = createJavaWebSocketContainer(webSocketUrl);
		webSocketClient.connect();
	}

	public void sendMessage(SlackMessage message) {

		// turn the payload into a string
		JsonNode node = objectMapper.valueToTree(message);
		String payload = node.toString();

		// send the payload and pray
		webSocketClient.send(payload);
	}

	@Override
	public void onOpen(ServerHandshake serverHandshake) {
		System.out.println("OPENED");
		isConnected = true;
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
		isConnected = false;
	}

	@Override
	public void onError(Exception e) {
		System.out.println("on error: "+e.getStackTrace());
	}

	/**
	 * Create a {@link WebSocketClient} with the given endpoint.
	 *
	 * @param endpoint the endpoint
	 * @return a WebSocketClient
	 */
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

		wsClient.setWebSocketFactory(new DefaultSSLWebSocketClientFactory(sslContext));
		return wsClient;
	}

	/**
	 * Convert a request to a json node.
	 * @param request the request
	 * @return the node
	 */
	private JsonNode executeRequestToJsonNode(HttpUriRequest request) {

		// get the json data
		JsonNode json = null;
		String content;
		CloseableHttpResponse response = null;
		try {
			response = httpClient.execute(request);
			content = EntityUtils.toString(response.getEntity(), EXPECTED_CHARSET);
			json = objectMapper.readTree(content);
		} catch(IOException ioe) {
			System.out.println("EXCEPTION: "+ioe); // TODO: fix
		} finally {
			try {
				if (response!=null) {
					response.close();
				}
			} catch(IOException ioe) {
				System.out.println("EXCEPTION: "+ioe); // TODO: fix
			}
		}

		// return it
		if (json==null) {
			System.out.println("no json to return, wat"); // TODO: fix
		}
		return json;
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

	public void setConnectionUrl(String connectionUrl) {
		this.connectionUrl = connectionUrl;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public void setHttpClient(CloseableHttpClient httpClient) {
		this.httpClient = httpClient;
	}

	public void setObjectMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

}
