package com.greenasjade.godojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

// This is a DTO carrying information about a change to a position with all required context.

@Data
public class AuditDTO {

    private final String placement;

    private Long node_id;

    private Long user_id;

    private String comment;

    private String field;

    private String original_value;

    private String new_value;

    public AuditDTO(Audit a) {
        this.placement = a.ref.getPlacement();
        this.node_id = a.ref.id;
        this.user_id = a.getUserId();
        this.comment = a.getComment();
        this.field = a.getField();
        this.original_value = a.getOriginalValue();
        this.new_value = a.getNewValue();
    }
}
