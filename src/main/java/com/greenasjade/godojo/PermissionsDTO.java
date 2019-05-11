package com.greenasjade.godojo;

import lombok.Data;

import java.time.Instant;

@Data
public class PermissionsDTO {
    private Boolean can_edit;

    private Boolean is_admin;

    public PermissionsDTO(User the_user) {
        this.can_edit = the_user.canEdit();
        this.is_admin = the_user.isAdministrator();
    }
}
