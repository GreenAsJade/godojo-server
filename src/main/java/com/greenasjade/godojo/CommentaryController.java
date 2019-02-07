package com.greenasjade.godojo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.Resource;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@RestController
public class CommentaryController {

	@Value("${godojo.http.ogs-key}")
	private String ogs_key;

    private static final Logger log = LoggerFactory.getLogger(CommentaryController.class);

    private BoardPositionStore bp_store;

    public CommentaryController(
            BoardPositionStore bp_store) {
        this.bp_store = bp_store;
    }

    @CrossOrigin()
    @ResponseBody()
    @GetMapping("/commentary" )
    // Return all the information needed to display commentary for a position
    public CommentaryDTO commentary(
            @RequestParam(value = "id", required = false, defaultValue = "root") String id) {

        BoardPosition board_position;

        log.info("Commentary request for: " + id);

        if (id.equals("root")) {
            board_position = this.bp_store.findByPlay(".root");
            id = board_position.id.toString();
        }
        else {
            board_position = this.bp_store.findById(Long.valueOf(id)).orElse(null);
        }

        log.info("which is: " + board_position.toString());

        log.info("with comments " + board_position.getCommentCount().toString());

        CommentaryDTO commentary = new CommentaryDTO(board_position);

        //log.info("DTO is " + commentary.toString());

        commentary.add(linkTo(methodOn(CommentaryController.class).commentary(id)).withSelfRel());

        return commentary;
    }

    @CrossOrigin()
    @ResponseBody()
    @PostMapping("/comment")
    // Update details about a given position
    public CommentaryDTO addComment(
            @RequestHeader("X-User-Info") String user_jwt,
            @RequestParam(value="id", required=true) String id,
            @RequestBody String comment) {

        // Grab the user-id off the jwt to store as the "commenter"
        // (throw and die if it's not valid)
        Jwt token = JwtHelper.decodeAndVerify(user_jwt, new RsaVerifier(ogs_key));

        String claims = token.getClaims();

        JsonNode jwtClaims = null;
        try {
            jwtClaims = new ObjectMapper().readTree(claims);
        } catch (java.io.IOException e) {
            return null;
        }

        Integer user_id = jwtClaims.get("user_id").asInt();

        BoardPosition the_position = this.bp_store.findById(Long.valueOf(id)).orElse(null);

        the_position.addComment(comment, user_id);

        this.bp_store.save(the_position);

        return new CommentaryDTO(the_position);
    }
}