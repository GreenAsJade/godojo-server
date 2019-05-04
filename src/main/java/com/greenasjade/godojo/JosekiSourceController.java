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
public class JosekiSourceController {

	@Value("${godojo.http.ogs-key}")
	private String ogs_key;

    private static final Logger log = LoggerFactory.getLogger(JosekiSourceController.class);

    private JosekiSources store;

    public JosekiSourceController(
            JosekiSources store) {
        this.store = store;
    }

    @CrossOrigin()
    @ResponseBody()
    @GetMapping("/godojo/josekisources" )
    // Return the list of all joseki sources
    public JosekiSourcesDTO josekisources() {

        log.info("Joseki sources request");

        return new JosekiSourcesDTO(store.listSources());
    }

    @CrossOrigin()
    @ResponseBody()
    @PostMapping("/godojo/josekisources")
    // Add a sequence of positions (creating new ones only as necessary)
    // The sequence is assumed to be based at the root (empty board)
    // The incoming move DTO describes the category for all new positions/moves that have to be created
    public JosekiSourceDTO createJosekiSource(
            @RequestHeader("X-User-Info") String user_jwt,
            @RequestBody JosekiSourceDTO source_info) {

        // Grab the user-id off the jwt to store as the "contributor"
        // (throw and die if it's not valid)
        Jwt token = JwtHelper.decodeAndVerify(user_jwt, new RsaVerifier(ogs_key));

        String claims = token.getClaims();

        JsonNode jwtClaims = null;
        try {
            jwtClaims = new ObjectMapper().readTree(claims);
        } catch (java.io.IOException e) {
            return null;
        }

        // log.info("Claims: " + jwtClaims.toString());

        Long user_id = jwtClaims.get("user_id").asLong();

        log.info("Saving josekisource for user: " + user_id.toString());

        if (source_info == null) {
            log.warn("WHOA NO SOURCE INFO! (brace for impact)");
        }
        else {
            log.info("Description: "  + source_info.getSource().getDescription());
            log.info("URL: " + source_info.getSource().getUrl());
        }
        JosekiSource the_new_source = new JosekiSource(
                source_info.getSource().getDescription(),
                source_info.getSource().getUrl(),
                user_id);

        store.save(the_new_source);

        JosekiSourceDTO new_source = new JosekiSourceDTO(the_new_source);

        // Finally, return the info for the created joseki source.
        return new_source;
    }

}