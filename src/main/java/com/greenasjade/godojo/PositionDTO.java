package com.greenasjade.godojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class PositionDTO {

    private String description;
    public String getDescription() {return description;}
    public void setDescription(String description) {this.description = description;}

    private PlayCategory category;
    public PlayCategory getCategory() {return category;}

    private String placement;
    public String getPlacement() {return placement;}

    private String play;
    public String getPlay() {return play;}

    private Integer contributor;
    public Integer getContributor() {return contributor;}

    private Long node_id;
    public Long getNodeId() {return node_id;}

    private Integer comment_count;
    public Integer getCommentCount() {return comment_count;}

    private ArrayList<MoveDTO> next_moves;
    public ArrayList<MoveDTO> getNextMoves() {return next_moves;}

    private MoveDTO parent;
    public MoveDTO getParent() {return parent;}

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
    }
}
