package com.greenasjade.godojo;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class PositionController {

	@Value("${godojo.http.ogs-key}")
	private String ogs_key;
	  
    private static final Logger log = LoggerFactory.getLogger(PositionController.class);

    private BoardPositionStore bp_store;

    public PositionController(
            BoardPositionStore bp_store) {
        this.bp_store = bp_store;
    }

    @CrossOrigin()
    @ResponseBody()
    @GetMapping("/position" )
    // Return all the information needed to display a position
    public PositionDTO position(
            @RequestParam(value = "id", required = false, defaultValue = "root") String id) {

        BoardPosition board_position;

        log.info("Position request for: " + id);

        if (id.equals("root")) {
            board_position = this.bp_store.findByPlay(".root");
            id = board_position.id.toString();
        }
        else {
            board_position = this.bp_store.findById(Long.valueOf(id)).orElse(null);
        }

        log.info("which is: " + board_position.toString());
        log.info("with comments " + board_position.getCommentCount().toString());

        PositionDTO position = new PositionDTO(board_position, bp_store);

        return position;
    }

    @CrossOrigin()
    @ResponseBody()
    @PostMapping("/positions")
    // Add a sequence of positions (creating new ones only as necessary)
    // The sequence is assumed to be based at the root (empty board)
    // The incoming move DTO describes the category for all new positions/moves that have to be created
    public PositionDTO createPositions(
            @RequestHeader("X-User-Info") String user_jwt,
            @RequestBody SequenceDTO sequence_details) {

        // Grab the user-id off the jwt to store as the "contributor"
        // (throw and die if it's not valid)
    	Jwt token = JwtHelper.decodeAndVerify(user_jwt, new RsaVerifier(ogs_key));

        String claims = token.getClaims();

        JsonNode jwtClaims = null;
        try {
            jwtClaims = new ObjectMapper().readTree(claims);
        } catch (java.io.IOException e) {
            return null;
        }

        // log.info("Claims: " + jwtClaims.toString());

        Long user_id = jwtClaims.get("user_id").asLong();

        log.info("Saving sequence for user: " + user_id.toString());

        // Now, first we have to find where the first new move to be created is in the sequence they gave us
                
        List<String> placements = new ArrayList<>(Arrays.asList(sequence_details.getSequence().split(",")));

        log.info("Adding move from sequence: " + placements.toString());

        BoardPosition current_position = bp_store.findByPlay(".root");
        String next_placement = placements.remove(0);
    	String next_play = ".root." + next_placement;
    	BoardPosition next_position = bp_store.findByPlay(next_play);
    	
        while (next_position != null && placements.size() > 0) {
        	log.info("found existing next position as: " + next_position);
        	current_position = next_position;
        	next_placement = placements.remove(0);
        	next_play = next_play +  '.' + next_placement;
        	log.info("looking for play: " + next_play);
        	next_position = bp_store.findByPlay(next_play);
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
       
        this.bp_store.save(next_position);

        // Finally, return the info for the last position created.
        return this.position(next_position.id.toString());
    }

    @CrossOrigin()
    @ResponseBody()
    @PutMapping("/position")
    // Update details about a given position
    public PositionDTO updatePosition(
            @RequestHeader("X-User-Info") String user_jwt,
            @RequestParam(value="id", required=true) String id,
            @RequestBody PositionDTO position_details) {

        // Grab the user-id off the jwt to store as the "editor"
        // (throw and die if it's not valid)
        Jwt token = JwtHelper.decodeAndVerify(user_jwt, new RsaVerifier(ogs_key));

        String claims = token.getClaims();

        JsonNode jwtClaims = null;
        try {
            jwtClaims = new ObjectMapper().readTree(claims);
        } catch (java.io.IOException e) {
            return null;
        }

        Integer user_id = jwtClaims.get("user_id").asInt();

        // TBD add audit of change

        BoardPosition the_position = this.bp_store.findById(Long.valueOf(id)).orElse(null);

        the_position.setDescription(position_details.getDescription());

        if (position_details.getCategory() != null) {
            the_position.setCategory(position_details.getCategory());
        }

        this.bp_store.save(the_position);

        return this.position(id);
    }
}