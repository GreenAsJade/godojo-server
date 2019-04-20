package com.greenasjade.godojo;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

@Data
public class ChangesDTO {

    private static final Logger log = LoggerFactory.getLogger(ChangesDTO.class);

    private ArrayList<AuditDTO> changes;

    public String toString() {
        return changes.toString();
    }

    // Outbound audit information
    public ChangesDTO(ArrayList<Audit> all_changes) {
        log.info("Building changes DTO");
        log.info(all_changes.toString());
        changes =  all_changes.stream()
                        .sorted( Comparator.comparing(Audit::getDate))
                        .map(AuditDTO::new)
                        .collect(Collectors.toCollection(ArrayList::new));
    }
}
