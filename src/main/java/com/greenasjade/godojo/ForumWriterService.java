package com.greenasjade.godojo;

import io.sentry.Sentry;
import io.sentry.event.BreadcrumbBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


@Service
public class ForumWriterService {

    private static Logger log = LoggerFactory.getLogger(ForumWriterService.class);

    @Value("${godojo.server.url}")
    private String serverUrl;

    @Value("${godojo.server.api}")
    private String serverApi;

    @Value("${godojo.forum.url}")
    private String forumUrl;

    @Value("${godojo.forum.joseki-category}")
    private Integer JOSEKI_CATEGORY;  // The forum category ID for Joseki posts

    @Value("${godojo.forum.joseki-user}")
    private String JOSEKI_POST_USER;

    @Value("${godojo.forum.joseki-user-key}")
    private String JOSEKI_POST_API_KEY;

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

    @Autowired
    private HttpHeaders apiHeaders;

    @Bean
    public HttpHeaders apiHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("User-Agent", "Godojo Server");
        headers.set("Accept", "*/*");
        return headers;
    }

    public String fetchContributorName(Long contributorId) {
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity request = new HttpEntity(apiHeaders);
        String apiUrl = serverApi + "/v1/players/" + contributorId.toString();
        J01Application.debug("Fetching contributor name from " + apiUrl, log);

        ResponseEntity<UserGetResponseDTO> response = restTemplate.exchange(
                apiUrl, HttpMethod.GET, request, UserGetResponseDTO.class);

        return response.getBody().getUsername();
    }

    @Async("asyncExecutor")
    public void startPositionTopic(BoardPosition position, String comment, String commenterName) {
        String play = position.getPlay();

        J01Application.debug("Starting forum topic for: " + play, log);

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

        String position_url = serverUrl + "/" + position.id + "?show_comments=true";

        String contributorName = fetchContributorName(position.getContributorId());

        String raw_post_text =
                "At position [" + play + "](" + position_url + "), " +
                "@" + commenterName + " started the conversation: \n\n" +
                comment + "\n\n";

        if (contributorName != null) {
            raw_post_text += "( FYI @" + contributorName + " )";
        }

        // log.debug(raw_post_text);

        JSONObject topicObject = new JSONObject();
        try {
            topicObject.put("category", JOSEKI_CATEGORY);
            topicObject.put("title", "Discussion about " + play);
            topicObject.put("raw", raw_post_text);
        }
        catch (JSONException e) {
            log.error("Can't create topic for " + play);
            log.error(e.toString());
            return;
        }

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity request = new HttpEntity(topicObject.toString(), forumHeaders);

        J01Application.debug("About to post: " + forumUrl + "\nheaders: " + forumHeaders.toString() + "\nrequest: " + request.toString(), log);

        ForumPostResponseDTO response = restTemplate.postForObject(
                forumUrl + "/posts.json",
                request,
                ForumPostResponseDTO.class);

        J01Application.debug("Forum server result: " +  response, log);

        if (response != null) {
            position.setForumThreadId(response.getTopicId());
            bp_access_native.save(position);
        }
    }

    @Async("asyncExecutor")
    public void addPositionComment(BoardPosition position, String comment, String commenterName) {
        String play = position.getPlay();
        J01Application.debug("Adding forum comment for: " + play, log);

        // Quote the comment:
        comment = "> " + comment;
        comment = comment.replace("\n", "\n> ");

        String position_url = serverUrl + "/" + position.id + "?show_comments=true";

        String contributorName = fetchContributorName(position.getContributorId());

        String raw_post_text =
                "@" + commenterName + " [said](" + position_url + "): \n\n" +
                comment + "\n\n";

        if (contributorName != null) {
            raw_post_text += "( FYI @" + contributorName + " )";
        }

        // log.debug(raw_post_text);

        JSONObject topicObject = new JSONObject();
        try {
            topicObject.put("topic_id", position.getForumThreadId());
            topicObject.put("raw", raw_post_text);
        }
        catch (JSONException e) {
            log.error("Can't create post for " + play);
            log.error(e.toString());
            return;
        }

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity request = new HttpEntity(topicObject.toString(), forumHeaders);

        J01Application.debug("About to post: " + forumUrl + "\nheaders: " + forumHeaders.toString() + "\nrequest: " + request.toString(), log);

        ForumPostResponseDTO response = restTemplate.postForObject(
                forumUrl + "/posts.json",
                request,
                ForumPostResponseDTO.class);

        J01Application.debug("Forum server result: " +  response, log);

    }

}
