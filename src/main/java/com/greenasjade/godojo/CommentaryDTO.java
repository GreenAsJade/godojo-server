package com.greenasjade.godojo;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

@Data
public class CommentaryDTO {

    private static final Logger log = LoggerFactory.getLogger(CommentaryDTO.class);

    private ArrayList<Comment> commentary;

    public String toString() {
        return commentary.toString();
    }


    // Outbound comment information
    public CommentaryDTO(BoardPosition position) {
        // BoardPositions can have null elements returned from neo4j queries
        // BoardPosition commentary array returned from neo is not sorted
        log.info("Building commentary dto for " + position);
        if (position.commentary != null) {
            commentary = position.commentary.stream().
                    sorted( Comparator.comparing(Comment::getDate))
                    .collect(Collectors.toCollection(ArrayList::new));
        }
        else {
            commentary =  new ArrayList<>();
        }
    }
}
