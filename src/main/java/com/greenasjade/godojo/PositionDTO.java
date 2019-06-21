package com.greenasjade.godojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Data
public class PositionDTO {

    private static final Logger log = LoggerFactory.getLogger(PositionDTO.class);

    private String description;

    private Character variation_label;

    private PlayCategory category;

    private String placement;

    private String play;

    private Long contributor;

    private Long node_id;

    private Integer comment_count;

    private ArrayList<MoveDTO> next_moves;

    private MoveDTO parent;

    private List<String> tags;

    // incoming from client, telling us which one to use
    public Long joseki_source_id;

    // outgoing telling client all details
    private JosekiSource joseki_source;

    // Inbound position information
    @JsonCreator
    public PositionDTO(
            @JsonProperty("description") String description,
            @JsonProperty("variation_label") Character variation_label,
            @JsonProperty("category") String category,
            @JsonProperty("joseki_source_id") Long joseki_source_id) {
        this.description = description;
        this.variation_label = variation_label;
        // empty category means "don't change it"
        this.category = category == null ? null : PlayCategory.valueOf(category);
        this.joseki_source_id = joseki_source_id;
    }

    // Outbound position information
    public PositionDTO(BoardPosition position, BoardPositions bp_access) {
        description = position.getDescription();
        variation_label = position.getVariationLabel();
        category = position.getCategory();
        placement = position.getPlacement();
        contributor = position.getContributorId();
        play = position.getPlay();
        node_id = position.id;
        comment_count = position.getCommentCount();

        // The list of next moves from this position - have to get that from the DB
        List<BoardPosition> next_move_list = bp_access.findByParentId(position.id);

        next_moves = new ArrayList<>();
        if (next_move_list != null) {
            next_move_list.forEach( (move) -> {
                MoveDTO dto = new MoveDTO(move);
                next_moves.add(dto);
            });
        }

        joseki_source = position.source;

        joseki_source_id = position.source != null ?  position.source.getId() : 0L;

        // A link to the parent of the node we are telling them about, so they can go back from here
        BoardPosition parent_position = position.getPlay().equals(".root") ?
                position : position.parent;
        parent = new MoveDTO(parent_position);

        tags = new ArrayList<>();
    }
}
