package com.greenasjade.godojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.neo4j.ogm.annotation.GeneratedValue;

import java.time.Instant;

// This is a DTO carrying information about a change to a position with all required context.

@Data
public class AuditDTO {

    private Long _id; // for convenience to make react-table easy

    private final String placement;

    private Long node_id;

    private Long user_id;

    private String comment;

    private String original_value;

    private String new_value;

    private Instant date;

    public AuditDTO(Audit a) {
        this._id = a.id;
        this.date = a.getDate();
        this.placement = a.ref.getPlacement();
        this.node_id = a.ref.id;
        this.user_id = a.getUserId();
        this.comment = a.getComment();
        this.original_value = a.getOriginalValue();
        this.new_value = a.getNewValue();
    }
}
