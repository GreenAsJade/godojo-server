package com.greenasjade.godojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PositionDTO {

    private String description;

    private PlayCategory category;

    private String placement;

    private String play;

    private Long contributor;

    private Long node_id;

    private Integer comment_count;

    private ArrayList<MoveDTO> next_moves;

    private MoveDTO parent;

    private List<String> labels;

    // Inbound position information
    @JsonCreator
    public PositionDTO(
            @JsonProperty("description") String description,
            @JsonProperty("category") String category) {
        this.description = description;
        // empty move_type means "don't change it"
        this.category = category.equals("") ? null : PlayCategory.valueOf(category);
    }

    // Outbound position information
    public PositionDTO(BoardPosition position, BoardPositionStore bp_store) {
        description = position.getDescription();
        category = position.getCategory();
        placement = position.getPlacement();
        contributor = position.getContributorId();
        play = position.getPlay();
        node_id = position.id;
        comment_count = position.getCommentCount();

        // The list of next moves from this position - have to get that from the DB
        List<BoardPosition> next_move_list = bp_store.findByParentId(position.id);

        next_moves = new ArrayList<>();
        if (next_move_list != null) {
            next_move_list.forEach( (move) -> {
                MoveDTO dto = new MoveDTO(move);
                next_moves.add(dto);
            });

        }

        // A link to the parent of the node we are telling them about, so they can go back from here
        BoardPosition parent_position = position.getPlay().equals(".root") ?
                position : position.parent;
        parent = new MoveDTO(parent_position);

        labels = new ArrayList<>();
    }
}
