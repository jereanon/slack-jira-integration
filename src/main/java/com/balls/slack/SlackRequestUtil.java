package com.balls.slack;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * Helper methods for making HTTP requests
 */
public class SlackRequestUtil {
    private static CloseableHttpClient httpClient = null;
    private static ObjectMapper objectMapper = null;

    private static final Charset EXPECTED_CHARSET = Charset.forName("UTF-8");

    /**
     * Convert a request to a json node.
     * @param request the request
     * @return the node
     */
    public static JsonNode executeRequestToJsonNode(HttpUriRequest request) {
        // get the json data
        JsonNode json = null;
        String content;
        CloseableHttpResponse response = null;
        try {
            response = getHttpClient().execute(request);
            content = EntityUtils.toString(response.getEntity(), EXPECTED_CHARSET);
            json = getObjectMapper().readTree(content);
        } catch (IOException ioe) {
            System.out.println("EXCEPTION: " + ioe); // TODO: fix
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException ioe) {
                System.out.println("EXCEPTION: " + ioe); // TODO: fix
            }
        }

        // return it
        if (json == null) {
            System.out.println("no json to return, wat"); // TODO: fix
        }
        return json;
    }

    public static String mapToJsonString(Map<String, Object> map) {
        String jsonString = null;
        try {
            jsonString = objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            System.err.println(e.getMessage());
        }

        return jsonString;
    }

    private static CloseableHttpClient getHttpClient() {
        if (httpClient == null) {
            httpClient = HttpClients.createDefault();
        }

        return httpClient;
    }

    private static ObjectMapper getObjectMapper() {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper(new JsonFactory());
        }

        return objectMapper;
    }
}
