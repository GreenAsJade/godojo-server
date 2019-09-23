package com.greenasjade.godojo;

import org.neo4j.ogm.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.Data;

@Data
@RelationshipEntity(type = "PLAYED")
public class PlayRecord {
    @Id @GeneratedValue   private Long relationshipId;

    @StartNode private User user;
    @EndNode   private BoardPosition position;

    @Property private Integer attempts;
    @Property private Integer best_attempt;
    @Property private Integer successes;

    public PlayRecord(User user, BoardPosition position) {
        this.user = user;
        this.position = position;
        this.attempts = 0;
        this.best_attempt = -1;
        this.successes = 0;
    }

    public String toString() {
        return String.format("PlayRecord: %s -> %s", this.user.getUserId().toString(), this.position.id.toString());
    }
}
