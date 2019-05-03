package com.greenasjade.godojo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;

public class User {

    private static final Logger log = LoggerFactory.getLogger(User.class);

    //@Value("${godojo.http.ogs-key}")
    private String ogs_key="-----BEGIN PUBLIC KEY-----\n" +
            "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAIwcsDli8iZtiIV9VjKXBxsmiSGkRCf3\n" +
            "vi6y3wIaG7XDLEaXOzMEHsV8s+oRl2VUDc2UbzoFyApX9Zc/FtHEi1MCAwEAAQ==\n" +
            "-----END PUBLIC KEY-----";

    private Long id;
    public Long getId() {return id;}

    public User(String user_jwt) {
        // Grab the user-id off the jwt to store as the "contributor"
        // (throw and die if it's not valid)

        // log.info("User constructor: " + user_jwt + ogs_key);
        Jwt token = JwtHelper.decodeAndVerify(user_jwt, new RsaVerifier(ogs_key));

        String claims = token.getClaims();

        JsonNode jwtClaims;

        try {
            jwtClaims = new ObjectMapper().readTree(claims);
        } catch (java.io.IOException e) {
            id = null;   // urk should have a better solution TBD
            return;
        }

        //log.info("Claims: " + jwtClaims.toString());

        id = jwtClaims.get("user_id").asLong();
    }
}
