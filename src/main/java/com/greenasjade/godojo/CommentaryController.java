package com.greenasjade.godojo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class CommentaryController {

    private static final Logger log = LoggerFactory.getLogger(CommentaryController.class);

    private BoardPositions bp_access;
    private UserFactory user_factory;

    @Autowired
    private ForumWriterService forumWriterService;

    public CommentaryController(
            BoardPositionsNative bp_access,
            UserFactory user_factory) {
        this.bp_access = new BoardPositions(bp_access);
        this.user_factory = user_factory;
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
            board_position = this.bp_access.findActiveByPlay(".root");
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
    // Add a comment  about a given position
    public CommentaryDTO addComment(
            @RequestHeader("X-User-Info") String user_jwt,
            @RequestParam(value="id") String id,
            @RequestBody String comment) {

        BoardPosition the_position = this.bp_access.findById(Long.valueOf(id));

        User the_commenter = this.user_factory.createUser(user_jwt);

        if (the_commenter.canComment()) {
            the_position.addComment(comment, the_commenter.getUserId());

            this.bp_access.save(the_position);

            if (the_position.commentary.size() == 1) {
                forumWriterService.startPositionTopic(
                        the_position,
                        comment,
                        the_commenter.username

                );
            }
        }

        return new CommentaryDTO(the_position);
    }
}