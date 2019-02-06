package com.greenasjade.godojo;

import java.util.ArrayList;

public class CommentaryDTO extends HalResource {

    private ArrayList<Comment> commentary;
    public ArrayList<Comment> getCommentary() {return commentary;}

    public String toString() {
        return commentary.toString();
    }
    // Outbound comment information
    public CommentaryDTO(BoardPosition position) {
        // BoardPositions can have null elements returned from neo4j queries
        commentary = position.commentary != null ? position.commentary : new ArrayList<>();
    }
}
