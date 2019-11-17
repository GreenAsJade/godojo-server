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

import java.util.ArrayList;

/* This "factory" implements the logic to find out whether a user needs to be created
   in the DB or just returned as a new object from an existing user in the DB.

   It's an "java object factory" not a DB-entity creation thingo.
 */

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

    /*
       These "Create" methods create User objects, with info from the database if available.
       They don't create user instances in the database.
     */

    // Here we get an instance of a User by id so we can set properties on it and save those
    // for simple read-modify-write, and not relationship management
    User createUser(Long id) {
        User the_user = user_access.findByUserId(id);

        if (the_user == null) {
            the_user = new User(id);
        }

        J01Application.debug(the_user.toString(), log);
        return the_user;
    }

    // Here we fully populate the User object with its relationships from the DB
    // and with a temporary value for their current user name, from the jwt
    User createUser(String jwt) {
        J01Application.debug("Processing user jwt: " + jwt, log);

        JsonNode jwtClaims;

        try {
            Jwt token = JwtHelper.decodeAndVerify(jwt, new RsaVerifier(ogs_key));

            String claims = token.getClaims();

            jwtClaims = new ObjectMapper().readTree(claims);

        } catch (Exception e) {
            // Let's just carry on with them as anonymous

            log.error("Couldn't get user info from jwt - treating them as anonymous! " + e.toString());

            User the_user = new User(0L);
            the_user.setCanComment(false);  // anonymous can't comment
            return the_user;
        }

        J01Application.debug("User Claims: " + jwtClaims.toString(), log);

        Long id = jwtClaims.get("id").asLong();

        String username = jwtClaims.get("username").asText();

        User the_user = user_access.loadUserWithPlayRecordByUserId(id);

        if (the_user == null) {
            if (!jwtClaims.get("anonymous").asBoolean()) {
                the_user = new User(id);
            }
            else {
                J01Application.debug("anonymous visitor", log);
                the_user = new User(0L);
                the_user.setCanComment(false);  // anonymous can't comment
            }
        }

        the_user.username = username;

        if (the_user.played_josekis == null) {
            the_user.played_josekis = new ArrayList<>();
        }

        J01Application.debug(the_user.toString(), log);

        return the_user;
    }
}
