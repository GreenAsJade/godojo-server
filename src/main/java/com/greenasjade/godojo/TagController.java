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
public class TagController {
    private static final Logger log = LoggerFactory.getLogger(TagController.class);

    private Tags store;

    public TagController(
            Tags store) {
        this.store = store;
    }

    @CrossOrigin()
    @ResponseBody()
    @GetMapping("/godojo/tags" )
    // Return the list of all tags
    public TagsDTO josekisources() {

        log.info("Tags request");

        return new TagsDTO(store.listTags());
    }
}