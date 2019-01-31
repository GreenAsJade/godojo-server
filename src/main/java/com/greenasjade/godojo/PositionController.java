package com.greenasjade.godojo;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.Resource;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;

import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;

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

        PositionDTO position = new PositionDTO(board_position);
        position.add(linkTo(methodOn(PositionController.class).position(id)).withSelfRel());

        // Add the other resources we will supply in the response...

        ArrayList<Resource<MoveDTO>> resource_list = new ArrayList<>();

        // Add a "moves" link for each move on this board_position, so the client
        // can navigate through any move from this board_position

        List<BoardPosition> next_move_list = bp_store.findByParentId(board_position.id);
        if (next_move_list != null) {
            next_move_list.forEach( (move) -> {
                log.info("adding link to: " + move.toString());
                MoveDTO dto = new MoveDTO(move);
                Resource<MoveDTO> res = new Resource<>(dto);
                res.add(linkTo(methodOn(PositionController.class).
                        position(move.id.toString())).withSelfRel());
                resource_list.add(res);

            });
            position.embed("moves", resource_list);
        }

        // A link to the parent of the node we are telling them about, so they can go back from here
        BoardPosition parent_position = board_position.getPlay().equals(".root") ?
                board_position : board_position.parent;

        log.info("adding link parent: " + parent_position.toString());

        MoveDTO dto = new MoveDTO(parent_position);
        Resource<MoveDTO> res = new Resource<>(dto);
        res.add(linkTo(methodOn(PositionController.class).
                position(parent_position.id.toString())).withSelfRel());
        position.embed("parent", res);

        return position;
    }

    @CrossOrigin()
    @ResponseBody()
    @PostMapping("/position")
    // Add a position at the parent node based on the move_details
    public PositionDTO createPosition(
            @RequestParam(value="id", required=true) String parent_id,
            @RequestHeader("X-User-Info") String user_jwt,
            @RequestBody MoveDTO move_details) {
    	
    	Jwt token = JwtHelper.decodeAndVerify(user_jwt, new RsaVerifier(ogs_key));
    	
        String claims = token.getClaims();
        log.info(claims);
        
        BoardPosition parent_position = this.bp_store.findById(Long.valueOf(parent_id)).orElse(null);

        BoardPosition new_child = parent_position.addMove(move_details.getPlacement(), move_details.getCategory());

        this.bp_store.save(new_child);

        return this.position(new_child.id.toString());
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
    	
    	Jwt token = JwtHelper.decodeAndVerify(user_jwt, new RsaVerifier(ogs_key));
    	
        String claims = token.getClaims();
        log.info(claims);
        
        // So first we have to find where the first new move is
                
        List<String> placements = new ArrayList<>(Arrays.asList(sequence_details.getSequence().split(",")));
        
        log.info("Adding sequence: " + placements.toString());
        
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
    	next_position = current_position.addMove(next_placement, new_category);
    	
        while (placements.size() > 0) {
        	next_placement = placements.remove(0);
            log.info("Extending at: " + next_position + " with " + next_placement);
            next_position = next_position.addMove(next_placement, new_category);        	
        }
       
        this.bp_store.save(next_position);
        return this.position(next_position.id.toString());
    }

    @CrossOrigin()
    @ResponseBody()
    @PutMapping("/position")
    // Update details about a given position
    public PositionDTO updatePosition(
            @RequestParam(value="id", required=true) String id,
            @RequestBody PositionDTO position_details) {

        BoardPosition the_position = this.bp_store.findById(Long.valueOf(id)).orElse(null);

        the_position.setDescription(position_details.getDescription());

        if (position_details.getMoveType() != null) {
            the_position.setCategory(position_details.getMoveType());
        }

        this.bp_store.save(the_position);

        return this.position(id);
    }
}