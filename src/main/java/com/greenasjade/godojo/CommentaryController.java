package com.greenasjade.godojo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.web.bind.annotation.*;

@RestController
public class CommentaryController {

	@Value("${godojo.http.ogs-key}")
	private String ogs_key;

    private static final Logger log = LoggerFactory.getLogger(CommentaryController.class);

    private BoardPositions bp_access;

    public CommentaryController(
            BoardPositionsNative bp_access) {
        this.bp_access = new BoardPositions(bp_access);
    }

    @CrossOrigin()
    @ResponseBody()
    @GetMapping("/godojo/commentary" )
    // Return all the information needed to display commentary for a position
    public CommentaryDTO commentary(
            @RequestParam(value = "id", required = false, defaultValue = "root") String id) {

        BoardPosition board_position;

        log.info("Commentary request for: " + id);

        if (id.equals("root")) {
            board_position = this.bp_access.findByPlay(".root");
            id = board_position.id.toString();
        }
        else {
            board_position = this.bp_access.findById(Long.valueOf(id));
        }

        log.info("which is: " + board_position.toString());

        log.info("with comments " + board_position.getCommentCount().toString());

        CommentaryDTO commentary = new CommentaryDTO(board_position);

        return commentary;
    }

    @CrossOrigin()
    @ResponseBody()
    @PostMapping("/godojo/comment")
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

        Long user_id = jwtClaims.get("user_id").asLong();

        BoardPosition the_position = this.bp_access.findById(Long.valueOf(id));

        the_position.addComment(comment, user_id);

        this.bp_access.save(the_position);

        return new CommentaryDTO(the_position);
    }
}