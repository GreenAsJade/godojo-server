package com.greenasjade.godojo;

import lombok.Data;

import java.util.List;

@Data
public class TagsDTO {

    private List<Tag> tags;

    // Outbound response
    public TagsDTO(List<Tag> tags) {
        this.tags = tags;
    }
}
