package com.greenasjade.godojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

// This is a DTO for the Discourse topic in a topic_list https://docs.discourse.org/#tag/Categories%2Fpaths%2F~1categories.json%2Fpost

// I think we only care about the title (to find it) and the id (to add posts to it)

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ForumTopicListResponseDTO {

    private ForumTopicListDTO topic_list;

    public ForumTopicListResponseDTO(){}
}
