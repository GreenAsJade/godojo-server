package com.greenasjade.godojo;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

// This is a DTO for the audit information about a single position, designed to be
// displayed in the context of that position already established.
// So it doesn't carry information about the ref of each audit entry.

@Data
public class AuditLogDTO {

    private static final Logger log = LoggerFactory.getLogger(AuditLogDTO.class);

    private ArrayList<Audit> audits;

    public String toString() {
        return audits.toString();
    }

    // Outbound audit information
    public AuditLogDTO(BoardPosition position) {
        // BoardPositions can have null elements returned from neo4j queries
        // BoardPosition audits array returned from neo is not sorted, so have to sort them
        log.info("Building audit dto for " + position);
        audits =  (position.audits != null) ?
                position.audits.stream()
                        .sorted( Comparator.comparing(Audit::getDate))
                        .collect(Collectors.toCollection(ArrayList::new)) :
                new ArrayList<>();
    }
}
