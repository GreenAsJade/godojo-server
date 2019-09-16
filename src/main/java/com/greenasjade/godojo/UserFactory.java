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

/* This "factory" implements the logic to find out whether a user needs to be created
   in the DB or just returned as a new object from an existing user in the DB.

   It's an "object factory" not a DB-entity creation thingo.
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

    User createUser(Long id) {
        User the_user = user_access.findByUserId(id);

        if (the_user == null) {
            the_user = new User(id);
        }

        J01Application.debug(the_user.toString(), log);

        return the_user;
    }

    User createUser(String jwt) {
        // First grab the user-id off the jwt
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

        J01Application.debug("User Claims: " + jwtClaims.toString(), log);

        id = jwtClaims.get("id").asLong();

        String username = jwtClaims.get("username").asText();

        User the_user = user_access.findByUserId(id);

        if (the_user == null) {
            if (!jwtClaims.get("anonymous").asBoolean()) {
                the_user = new User(id);

                /* Example code to give permissions based on a rule...
                LocalDate joined_date = LocalDate.parse(jwtClaims.get("registration_date").asText(),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSxxxxx"));
                log.debug("User joined: " + joined_date.toString());

                LocalDate cutoff = LocalDate.parse("2019-01-01");

                if (joined_date.compareTo(cutoff) < 0) {
                    the_user.setCanEdit(true);
                }
                */

            } else {
                J01Application.debug("anonymous visitor", log);
                the_user = new User(0L);
                the_user.setCanComment(false);  // anonymous can't comment
            }
        }

        the_user.username = username;

        J01Application.debug(the_user.toString(), log);

        return the_user;
    }

    // This creates/updates a user entity in the database

    void updatePermissions(User target_user, PermissionsDTO permissions) {
        target_user.setCanComment(permissions.getCan_comment());
        target_user.setCanEdit((permissions.getCan_edit()));
        target_user.setAdministrator(permissions.getIs_admin());

        user_access.save(target_user);
    }
}
