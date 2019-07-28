package com.greenasjade.godojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

// This is a DTO for the OGS API Player GET response

// We actually only care about the username that we get back

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserGetResponseDTO {

    private String username;

    public UserGetResponseDTO(){}
}
