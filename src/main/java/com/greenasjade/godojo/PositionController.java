package com.greenasjade.godojo;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import org.springframework.hateoas.Resource;

@RestController
public class PositionController {

	private static final Logger log = LoggerFactory.getLogger(PositionController.class);

	private BoardPositionStore bp_store;
	//private JosekiStore j_store;
	private MoveStore m_store;

	public PositionController(
			BoardPositionStore bp_store,
			JosekiStore j_store,
			MoveStore m_store) {
		 this.bp_store = bp_store;
		 //this.j_store = j_store;
		 this.m_store = m_store;
	}

	@CrossOrigin()
	@ResponseBody()
    @GetMapping("/position" )
    public PositionDTO position(
            @RequestParam(value = "id", required = false, defaultValue = "root") String id) {

    	BoardPosition board_position;

    	log.info("Position request for: " + id);

    	if (id.equals("root")) {
    		board_position = this.bp_store.findByPlay("root");
    		id = board_position.id.toString();
    	}
    	else {
    		board_position = this.bp_store.findById(Long.valueOf(id)).orElse(null);
    	}

        log.info("which is: " + board_position.toString());

        PositionDTO position = new PositionDTO(board_position);
        position.add(linkTo(methodOn(PositionController.class).position(id)).withSelfRel());

        // Add a "moves" link for each move on this board_position, so the client
    	// can navigate through any move from this board_position

        List<Move> move_list = m_store.findByParentId(board_position.id);

        ArrayList<Resource<MoveDTO>> resource_list = new ArrayList<>();

        if (move_list != null) {
        	move_list.forEach( (move) -> {
        		log.info("adding link to: " + move.after.toString());
                MoveDTO dto = new MoveDTO(move);
                Resource<MoveDTO> res = new Resource<>(dto);
                res.add(linkTo(methodOn(PositionController.class).
        						position(move.after.id.toString())).withSelfRel());
                resource_list.add(res);

        	});
            position.embed("moves", resource_list);
        }

        return position;
    }

	@CrossOrigin()
	@ResponseBody()
	@PostMapping("/position")
	public PositionDTO createPosition(
			@RequestParam(value="id", required=true) String parent_id,
			@RequestBody MoveDTO move_details) {

		BoardPosition parent_position = this.bp_store.findById(Long.valueOf(parent_id)).orElse(null);

		BoardPosition new_child = parent_position.addMove(move_details.getPlacement(), move_details.getCategory());

		this.bp_store.save(new_child);

		return this.position(new_child.id.toString());
	}

	@CrossOrigin()
	@ResponseBody()
	@PutMapping("/position")
	public PositionDTO updatePosition(
			@RequestParam(value="id", required=true) String id,
			@RequestBody PositionDTO position_details) {

		BoardPosition the_position = this.bp_store.findById(Long.valueOf(id)).orElse(null);

		/* We only support updating the description at the moment */

		the_position.setDescription(position_details.getDescription());

		this.bp_store.save(the_position);

		return this.position(id.toString());
	}
}