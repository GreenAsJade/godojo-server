package com.greenasjade.godojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class PermissionsDTO {
    private Boolean can_comment;

    private Boolean can_edit;

    private Boolean is_admin;

    // Inbound position information
    @JsonCreator
    public PermissionsDTO(
            @JsonProperty("can_comment") Boolean can_comment,
            @JsonProperty("can_edit") Boolean can_edit,
            @JsonProperty("can_admin") Boolean can_admin) {
        this.can_comment = can_comment;
        this.can_edit = can_edit;
        this.is_admin = can_admin;
    }

    public PermissionsDTO(User the_user) {
        this.can_edit = the_user.canEdit();
        this.is_admin = the_user.isAdministrator();
        this.can_comment = the_user.canComment();
    }
}
