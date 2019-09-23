package com.greenasjade.godojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PlayRecordDTO {

    private Long position_id;
    private Integer error_count;

    //outbound, refers to player's whole history.
    private Integer josekis_played;
    private Integer josekis_completed;

    // outbound, refers to current sequence/position
    private Integer successes;

    // Inbound position information
    @JsonCreator
    public PlayRecordDTO(
            @JsonProperty("position_id") Long position_id,
            @JsonProperty("errors") Integer error_count) {
        this.position_id = position_id;
        this.error_count = error_count;
    }

    // Outbound info just about the user, no specific position
    public PlayRecordDTO(User the_user) {
        this.josekis_played = the_user.josekisPlayedCount();
        this.josekis_completed = (int)(long)the_user.completedJosekiCount();
    }

    // Outbound info to client about a position (and general user info)
    public PlayRecordDTO(PlayRecord play_record) {

        User the_user = play_record.getUser();
        BoardPosition the_position = play_record.getPosition();

        this.error_count = the_user.errorsFor(the_position.id);
        this.josekis_played = the_user.josekisPlayedCount();
        this.josekis_completed = (int)(long)the_user.completedJosekiCount();
        this.position_id = the_position.id;
        this.successes = play_record.getSuccesses();
    }
}
