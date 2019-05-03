package com.greenasjade.godojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RevertRequestDTO {

    final Long audit_id;

    @JsonCreator
    public RevertRequestDTO(
            @JsonProperty("audit_id") Long audit_id) {
        this.audit_id = audit_id;
    }
}
