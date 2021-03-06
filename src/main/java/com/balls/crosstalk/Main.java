package com.balls.crosstalk;

import com.balls.slack.SlackRealTimeMessagingConnection;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Run the CrossTalk server
 */
public class Main {
    public static void main(String[] args) {
        // Read in configuration data
        List<Map> config = null;
        try {
            InputStream input = new FileInputStream(new File("src/main/resources/config.yml"));
            Yaml yaml = new Yaml();
            config = (List<Map>) yaml.load(input);
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
            System.exit(0);
        }
        String slackUrl = "https://" + config.get(0).get("team") + ".slack.com";
        String slackApiKey = (String) config.get(0).get("apiKey");

        // Jack in to dat Slack
        SlackRealTimeMessagingConnection connection = new SlackRealTimeMessagingConnection(
                slackUrl,
                "crosstalk",
                slackApiKey,
                true);
        connection.startRealTimeClient();

        // Start yer engines!
        CrossTalkBot bot = new CrossTalkBot((String) config.get(0).get("restUrl"));
        connection.registerSlackMessageHandler(bot);
    }
}
