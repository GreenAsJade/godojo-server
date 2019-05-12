package com.greenasjade.godojo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class UserFactory {

    private static final Logger log = LoggerFactory.getLogger(UserFactory.class);

    @Value("${godojo.http.ogs-key}")
    private String ogs_key;

    private Users user_access;

    public UserFactory(
            Users user_access) {
        this.user_access = user_access;
    }

    User createUser(String jwt) {
        // First grab the user-id off the jwt

        log.info("Create user...");
        Jwt token = JwtHelper.decodeAndVerify(jwt, new RsaVerifier(ogs_key));

        String claims = token.getClaims();

        JsonNode jwtClaims;

        Long id;

        try {
            jwtClaims = new ObjectMapper().readTree(claims);
        } catch (java.io.IOException e) {
            log.error("Couldn't get user info!");
            return null;  // urk should have a better solution TBD
        }

        log.info("Claims: " + jwtClaims.toString());

        id = jwtClaims.get("id").asLong();

        User the_user = user_access.findByUserId(id);

        if (the_user == null) {
            if (!jwtClaims.get("anonymous").asBoolean()) {
                the_user = new User(id);
                the_user.setCanComment(true);

                LocalDate joined_date = LocalDate.parse(jwtClaims.get("registration_date").asText(),
                        DateTimeFormatter.ofPattern("yyyy-MM-DD HH:mm:ss.SSSSSSxxxxx"));
                log.info("User joined: " + joined_date.toString());

                LocalDate cutoff = LocalDate.parse("2019-01-01");

                if (joined_date.compareTo(cutoff) < 0) {
                    the_user.setCanEdit(true);
                }
            } else {
                log.info("anonymous visitor");
                the_user = new User(0L);
            }
        }

        log.info(the_user.toString());

        return the_user;
    }
}
