package com.greenasjade.godojo;

import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;


@RestController
public class BoardPositionController {
	  
    private static final Logger log = LoggerFactory.getLogger(BoardPositionController.class);

    private BoardPositions bp_access;
    private JosekiSources js_access;
    private Tags tag_access;
    private UserFactory user_factory;

    public BoardPositionController(
            BoardPositionsNative bp_native_access,
            JosekiSources js_access,
            Tags tag_access,
            UserFactory user_factory) {
        this.bp_access = new BoardPositions(bp_native_access);
        this.js_access = js_access;
        this.tag_access = tag_access;
        this.user_factory = user_factory;
    }

    @CrossOrigin()
    @ResponseBody()
    @GetMapping("/godojo/position" )
    // Return all the information needed to display a position
    // Filter out variations by contributor if contributor filter id is supplied
    public BoardPositionDTO position(
            @RequestParam(value = "id", required = false, defaultValue = "root") String id,
            @RequestParam(value="cfilterid", required = false) Long variation_contributor) {

        BoardPosition board_position;

        log.info("Position request for: " + id);

        if (id.equals("root")) {
            board_position = this.bp_access.findActiveByPlay(".root");
            id = board_position.id.toString();
        }
        else {
            board_position = this.bp_access.findById(Long.valueOf(id));
        }

        log.info("which is: " + board_position.getInfo());

        List<BoardPosition> next_positions;

        if (variation_contributor != null && variation_contributor != 0L) {
            log.info("filtering for variations by " + variation_contributor.toString());
            next_positions = bp_access.findRoutesOfContributor(board_position.id, variation_contributor);
            log.info("next positions: " + next_positions.toString());
        }
        else {
            // We have to read them here in case the incoming BoardPosition does not have them
            // already read from the DB
            next_positions = bp_access.findByParentId(board_position.id);
        }

        BoardPositionDTO position = new BoardPositionDTO(board_position, next_positions);

        return position;
    }

    @CrossOrigin()
    @ResponseBody()
    @PostMapping("/godojo/positions")
    // Add a sequence of positions (creating new ones only as necessary)
    // The supplied sequence is assumed to be based at the root (empty board)
    // The incoming Sequence DTO describes the category for all new positions/moves that have to be created
    // Only the category is set for all created positions - other parameters need a separate call
    // (to updatePosition) to set them.
    public BoardPositionDTO createPositions(
            @RequestHeader("X-User-Info") String user_jwt,
            @RequestBody SequenceDTO sequence_details) {

        User the_user = this.user_factory.createUser(user_jwt);

        Long user_id = the_user.getUserId();

        if (!the_user.canEdit()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, String.format("User %s does not have edit permissions", user_id.toString())
            );
        }

        log.info("Saving new move sequence from user: " + user_id.toString());
                
        List<String> placements = new ArrayList<>(Arrays.asList(sequence_details.getSequence().split(",")));

        log.info("Adding move from sequence: " + placements.toString());

        // Now, first we have to find where the first new move to be created is, in the sequence they gave us
        // So we start from "root" and see if each move in the sequence exists...

        BoardPosition current_position = bp_access.findActiveByPlay(".root");

        String next_placement = placements.remove(0);
    	String next_play = ".root." + next_placement;
    	BoardPosition next_position = bp_access.findActiveByPlay(next_play);
    	
        while (next_position != null && placements.size() > 0) {
        	log.info("found existing next position as: " + next_position);
        	current_position = next_position;
        	next_placement = placements.remove(0);
        	next_play = next_play +  '.' + next_placement;
        	log.info("looking for play: " + next_play);
        	next_position = bp_access.findActiveByPlay(next_play);
        }        	
            
        // Now "current_position" is an existing position, and next_placement takes us to the first new one to be created
        // so we add the remaining placements creating new positions as we go
        
        log.info("Extending at: " + current_position + " with " + next_placement);
        
        PlayCategory new_category = sequence_details.getCategory();

    	next_position = current_position.addMove(next_placement, new_category, user_id);

        while (placements.size() > 0) {
        	next_placement = placements.remove(0);
            log.info("Extending at: " + next_position + " with " + next_placement);
            next_position = next_position.addMove(next_placement, new_category, user_id);
        }
       
        this.bp_access.save(next_position);

        // Finally, return the info for the last position created.
        return this.position(next_position.id.toString(), null);
    }

    @CrossOrigin()
    @ResponseBody()
    @PutMapping("/godojo/position")
    // Update details about a given position
    public BoardPositionDTO updatePosition(
            @RequestHeader("X-User-Info") String user_jwt,
            @RequestParam(value="id") String id,
            @RequestBody BoardPositionDTO position_details) {

        log.info("updatePosition: " + position_details.toString());

        User the_user = this.user_factory.createUser(user_jwt);

        Long user_id = the_user.getUserId();

        if (!the_user.canEdit()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, String.format("User %s does not have edit permissions", user_id.toString())
            );
        }

        BoardPosition the_position = this.bp_access.findById(Long.valueOf(id));

        the_position.setDescription(position_details.getDescription(), user_id);

        the_position.setVariationLabel(position_details.getVariation_label());

        if (position_details.getCategory() != null) {
            the_position.setCategory(position_details.getCategory(), user_id);
        }

        if (position_details.joseki_source_id != null) {
            the_position.source = js_access.findById(position_details.joseki_source_id).orElse(null);
        }

        if (position_details.tag_ids != null) {
            the_position.setTags(position_details.tag_ids
                    .stream()
                    .map(tag_id -> tag_access.findById(tag_id).orElse(null))
                    .filter(t -> t != null)
                    .collect(Collectors.toList()));
        }

        this.bp_access.save(the_position);

        return this.position(id, null);
    }
}