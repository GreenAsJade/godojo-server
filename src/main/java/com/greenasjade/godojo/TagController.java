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
    public TagsDTO tags() {

        J01Application.debug("Tags request", log);

        return new TagsDTO(store.listTags());
    }

    @CrossOrigin()
    @ResponseBody()
    @GetMapping("/godojo/tag" )
    // Get a tag by group and sequence number
    // This is really only intended for getting 0,0 tag, which is intended to be
    // "The Joseki Tag".
    public TagsDTO tag(
            @RequestParam(value="group") Integer group,
            @RequestParam(value="seq") Integer seq) {
        J01Application.debug(String.format("Tag request %d %d", group, seq), log);

        return new TagsDTO(store.findTagByGroupSeq(group, seq));
    }


}