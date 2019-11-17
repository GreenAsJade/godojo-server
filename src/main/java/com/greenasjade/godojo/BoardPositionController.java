package com.greenasjade.godojo;

import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;


@RestController
public class BoardPositionController {
	  
    private static final Logger log = LoggerFactory.getLogger(BoardPositionController.class);

    private BoardPositions bp_access;
    private JosekiSources js_access;
    private Tags tag_access;
    private UserFactory user_factory;
    private AppInfos app_info_access;

    public BoardPositionController(
            BoardPositionsNative bp_native_access,
            JosekiSources js_access,
            Tags tag_access,
            UserFactory user_factory,
            AppInfos app_info_access) {
        this.bp_access = new BoardPositions(bp_native_access);
        this.js_access = js_access;
        this.tag_access = tag_access;
        this.user_factory = user_factory;
        this.app_info_access = app_info_access;
    }

    @CrossOrigin()
    @ResponseBody()
    @GetMapping("/godojo/position" )
    // Return all the information needed to display a position
    // Filter out variations as specified by params
    public ResponseEntity<BoardPositionDTO> position(
            @RequestHeader("X-User-Info") String user_jwt,
            @RequestParam(value = "id", required = false, defaultValue = "root") String id,
            @RequestParam(value="cfilterid", required = false) Long variation_contributor,
            @RequestParam(value="tfilterid", required = false) List<Long> variation_tags,
            @RequestParam(value="sfilterid", required = false) Long variation_source,
            @RequestParam(value="mode", required = false) String client_mode) {

        BoardPosition board_position;

        User the_user = this.user_factory.createUser(user_jwt);

        J01Application.debug("User " + the_user.username +  " position request for: " + id, log);

        if (client_mode != null) {
            J01Application.debug("mode " + client_mode, log);
        }
        AppInfo app_info = this.app_info_access.getAppInfo();

        if (id.equals("root")) {
            // note that we don't increment page visit count for root, it doesn't
            // really count as using the explorer.
            board_position = this.bp_access.findActiveByPlay(".root");
            id = board_position.id.toString();
        }
        else {
            app_info.incrementVisitCount(the_user, client_mode);
            this.app_info_access.save(app_info);

            board_position = this.bp_access.findById(Long.valueOf(id));

            if (board_position == null) {
                J01Application.debug("requested position does not exist", log);

                return new ResponseEntity(HttpStatus.NOT_FOUND);
            }

            if (board_position.parent == null && !board_position.getPlay().equals(".root")) {
                J01Application.debug("which is a deleted position.", log);

                return new ResponseEntity(HttpStatus.NOT_FOUND);
            }
        }

        J01Application.debug("which is: " + board_position.getInfo(), log);


        List<BoardPosition> next_positions;

        if (variation_tags != null || variation_contributor != null || variation_source != null) {
            List<Long> tagIds = null;

            if (variation_tags != null) {
                J01Application.debug("filtering for variations by tags " +
                        variation_tags.toString(), log);

                tagIds = new ArrayList<>();
                tagIds.addAll(variation_tags);
            }

           if (variation_contributor != null) {
               J01Application.debug("filtering for variations by contributor " +
                       variation_contributor.toString(), log);
           }

           if (variation_source != null) {
               J01Application.debug("filtering for variations by source " +
                       variation_source.toString(), log);
           }

            next_positions = bp_access.findFilteredVariations(board_position.id, variation_contributor, tagIds, variation_source);
            J01Application.debug("next positions: " + next_positions.toString(), log);
        }
        else {
            // Optimisation: if we don't have to ask the DB to do the big filter thing, then don't.
            // We have to read the next positions from DB here in case the incoming BoardPosition does not have them
            // already read from the DB
            next_positions = bp_access.findByParentId(board_position.id);
        }

        Integer child_count = bp_access.countChildren(board_position.id);

        BoardPositionDTO position = new BoardPositionDTO(board_position, next_positions, child_count, app_info.getLockedDown());

        return new ResponseEntity(position, HttpStatus.OK);
    }

    // an alias when we don't want to filter, used below.
    ResponseEntity<BoardPositionDTO> position(String user_jwt, String id) {
        return this.position(user_jwt, id, null, null, null, null);
    }

    @CrossOrigin()
    @ResponseBody()
    @PostMapping("/godojo/positions")
    // Add a sequence of positions (creating new ones only as necessary)
    // The supplied sequence is assumed to be based at the root (empty board)
    // The incoming Sequence DTO describes the category for all new positions/moves that have to be created
    // Only the category is set for all created positions - other parameters need a separate call
    // (to updatePosition) to set them.
    public ResponseEntity<BoardPositionDTO> createPositions(
            @RequestHeader("X-User-Info") String user_jwt,
            @RequestBody SequenceDTO sequence_details) {

        AppInfo app_info = this.app_info_access.getAppInfo();

        User the_user = this.user_factory.createUser(user_jwt);

        Long user_id = the_user.getUserId();

        if (!the_user.canEdit() || app_info.getLockedDown()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, String.format("User %s does not have edit permissions", user_id.toString())
            );
        }

        J01Application.debug("Saving new move sequence from user: " + user_id.toString(),log);
                
        List<String> placements = new ArrayList<>(Arrays.asList(sequence_details.getSequence().split(",")));

        J01Application.debug("Adding move from sequence: " + placements.toString(), log);

        // Now, first we have to find where the first new move to be created is, in the sequence they gave us
        // So we start from "root" and see if each move in the sequence exists...

        BoardPosition current_position = bp_access.findActiveByPlay(".root");

        String next_placement = placements.remove(0);
    	String next_play = ".root." + next_placement;
    	BoardPosition next_position = bp_access.findActiveByPlay(next_play);
    	
        while (next_position != null && placements.size() > 0) {
        	J01Application.debug("found existing next position as: " + next_position, log);
        	current_position = next_position;
        	next_placement = placements.remove(0);
        	next_play = next_play +  '.' + next_placement;
        	J01Application.debug("looking for play: " + next_play, log);
        	next_position = bp_access.findActiveByPlay(next_play);
        }        	
            
        // Now "current_position" is an existing position, and next_placement takes us to the first new one to be created
        // so we add the remaining placements creating new positions as we go
        
        J01Application.debug("Extending at: " + current_position + " with " + next_placement, log);
        
        PlayCategory new_category = sequence_details.getCategory();

    	next_position = current_position.addMove(next_placement, new_category, user_id);

        while (placements.size() > 0) {
        	next_placement = placements.remove(0);
            J01Application.debug("Extending at: " + next_position + " with " + next_placement, log);
            next_position = next_position.addMove(next_placement, new_category, user_id);
        }
       
        this.bp_access.save(next_position);

        // Finally, return the info for the last position created.
        return this.position(user_jwt, next_position.id.toString());
    }

    @CrossOrigin()
    @ResponseBody()
    @PutMapping("/godojo/position")
    // Update details about a given position
    public ResponseEntity<BoardPositionDTO> updatePosition(
            @RequestHeader("X-User-Info") String user_jwt,
            @RequestParam(value="id") String id,
            @RequestBody BoardPositionDTO position_details) {

        J01Application.debug("updatePosition: " + position_details.toString(), log);

        AppInfo app_info = this.app_info_access.getAppInfo();

        User the_user = this.user_factory.createUser(user_jwt);

        Long user_id = the_user.getUserId();

        if (!the_user.canEdit() || app_info.getLockedDown()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, String.format("User %s does not have edit permissions", user_id.toString())
            );
        }

        BoardPosition the_position = this.bp_access.findById(Long.valueOf(id));

        J01Application.debug("updatePosition - the_position" + the_position.toString(), log);

        the_position.setDescription(position_details.getDescription(), user_id);

        the_position.setVariationLabel(position_details.getVariation_label());

        the_position.setMarks(position_details.getMarks());

        if (position_details.getCategory() != null) {
            the_position.setCategory(position_details.getCategory(), user_id);
        }

        if (position_details.joseki_source_id != null) {
            J01Application.debug("sourcing joseki source " + position_details.joseki_source_id, log);
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

        return this.position(user_jwt, id);
    }

    @CrossOrigin()
    @ResponseBody()
    @GetMapping("/godojo/position/tagcount" )
    // Return all the information needed to display a position
    // Filter out variations as specified by params
    public Integer countOfTags(
            @RequestParam(value = "id", required = false, defaultValue = "root") String id,
            @RequestParam(value="tfilterid") Long variation_tag) {

        BoardPosition board_position;

        if (id.equals("root")) {
            board_position = this.bp_access.findActiveByPlay(".root");
        }
        else {
            board_position = this.bp_access.findById(Long.valueOf(id));
        }

        Integer result = bp_access.countChildrenWithTag(board_position.id, variation_tag);

        J01Application.debug("At node " + id + " tag id " + variation_tag + " count is " + result, log);

        return result;
    }

    @CrossOrigin()
    @ResponseBody()
    @GetMapping("/godojo/position/tagcounts" )
    // Return all the information needed to display a position
    // Filter out variations as specified by params
    public TagsDTO countsOfTags(
            @RequestParam(value = "id", required = false, defaultValue = "root") String id) {

        BoardPosition board_position;

        if (id.equals("root")) {
            board_position = this.bp_access.findActiveByPlay(".root");
        }
        else {
            board_position = this.bp_access.findById(Long.valueOf(id));
        }

        J01Application.debug("Tags count request for node " + board_position.toString(), log);

        List<Tag> tags = tag_access.listTags();

        tags.stream()
                .forEach( t ->
                    t.setContinuationCount(bp_access.countChildrenWithTag(board_position.id, t.id))
                );

        return new TagsDTO(tags);
    }


}