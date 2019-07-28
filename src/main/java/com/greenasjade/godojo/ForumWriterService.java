package com.greenasjade.godojo;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


@Service
public class ForumWriterService {

    private static Logger log = LoggerFactory.getLogger(ForumWriterService.class);

    final Integer JOSEKI_CATEGORY = 36;  // The forum category ID for Joseki posts
    final String JOSEKI_POST_USER = "JosekiDictionary";
    final String JOSEKI_POST_API_KEY = "29e30bc67baae2ee4df852ede3a32796668ff74459d5d6641473a4a373589e7a";

    @Autowired
    private BoardPositionsNative bp_access_native;

    @Autowired
    private HttpHeaders forumHeaders;

    @Bean
    public HttpHeaders forumHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Api-Username", JOSEKI_POST_USER);
        headers.set("Api-Key", JOSEKI_POST_API_KEY);
        headers.set("Content-Type", "application/json");
        headers.set("User-Agent", "Godojo Server");
        headers.set("Accept", "*/*");
        return headers;
    }

    @Async("asyncExecutor")
    public void startPositionTopic(BoardPosition position, String comment, String commenterName) {
        String play = position.getPlay();
        log.info("Starting forum topic for: " + play);

        // Tidy up the Play for readability
        if (".root".equals(play)) {
            play ="Empty Board";
        }
        else {
            play = play.replace(".root.", "");
        }

        play = play.replace(".", ",");

        // Quote the comment:
        comment = "> " + comment;
        comment = comment.replace("\n", "\n> ");

        String url = "https://beta.online-go.com/joseki/" + position.id;

        String raw_post_text =  "At position [" + play + "](" + url + "), " +
                "@" + commenterName + " started the conversation: \n\n" +
                comment + "\n\n" /* +
                            "(FYI @" + contributorName */;

        // log.info(raw_post_text);

        JSONObject topicObject = new JSONObject();
        try {
            topicObject.put("category", JOSEKI_CATEGORY);
            topicObject.put("title", "(test) Discussion about " + play);
            topicObject.put("raw", raw_post_text);
        }
        catch (JSONException e) {
            log.error("Can't create topic for " + play);
            log.error(e.toString());
            return;
        }

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity request = new HttpEntity(topicObject.toString(), forumHeaders);

        ForumPostResponseDTO response = restTemplate.postForObject(
                "https://forums.online-go.com/posts.json",
                request,
                ForumPostResponseDTO.class);

        log.info("Forum server result: " +  response);

        if (response != null) {
            position.setForumThreadId(response.getTopicId());
            bp_access_native.save(position);
        }
    }
}
