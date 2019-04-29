package com.greenasjade.godojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RevertResultDTO {

    private final String result;
    public RevertResultDTO(String result) {
        this.result = result;
    }
}
