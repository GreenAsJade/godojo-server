package com.greenasjade.godojo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
public class JosekiSourceController {

    private static final Logger log = LoggerFactory.getLogger(JosekiSourceController.class);

    private JosekiSources store;
    private UserFactory user_factory;

    public JosekiSourceController(
            JosekiSources store,
            UserFactory user_factory) {
        this.store = store;
        this.user_factory = user_factory;
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

        User the_user = this.user_factory.createUser(user_jwt);

        Long user_id = the_user.getUserId();

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