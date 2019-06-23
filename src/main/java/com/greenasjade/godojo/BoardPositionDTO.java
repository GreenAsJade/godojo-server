package com.greenasjade.godojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Data
public class BoardPositionDTO {

    private static final Logger log = LoggerFactory.getLogger(BoardPositionDTO.class);

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

    // incoming
    public List<Long> tag_ids;

    // outgoing
    private List<Tag> tags;

    // incoming from client, telling us which one to use
    public Long joseki_source_id;

    // outgoing telling client all details
    private JosekiSource joseki_source;

    // Inbound position information
    @JsonCreator
    public BoardPositionDTO(
            @JsonProperty("description") String description,
            @JsonProperty("variation_label") Character variation_label,
            @JsonProperty("category") String category,
            @JsonProperty("joseki_source_id") Long joseki_source_id,
            @JsonProperty("tags") ArrayList<Long> tags) {
        this.description = description;
        this.variation_label = variation_label;
        // empty category means "don't change it"
        this.category = category == null ? null : PlayCategory.valueOf(category);
        this.joseki_source_id = joseki_source_id;
        this.tag_ids = tags;
    }

    // Outbound position information


    public BoardPositionDTO(BoardPosition position, List<BoardPosition> next_positions) {

        description = position.getDescription();
        variation_label = position.getVariationLabel();
        category = position.getCategory();
        placement = position.getPlacement();
        contributor = position.getContributorId();
        play = position.getPlay();
        node_id = position.id;
        comment_count = position.getCommentCount();

        // We need a list of Move DTOs for the variations
        next_moves = new ArrayList<>();
        if (next_positions != null) {
            next_positions.forEach( (move) -> {
                MoveDTO dto = new MoveDTO(move);
                next_moves.add(dto);
            });
        }

        joseki_source = position.source;

        // A link to the parent of the node we are telling them about, so they can go back from here
        BoardPosition parent_position = position.getPlay().equals(".root") ?
                position : position.parent;
        parent = new MoveDTO(parent_position);

        tags = position.tags;
    }
}
