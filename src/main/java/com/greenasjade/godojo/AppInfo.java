package com.greenasjade.godojo;

import org.neo4j.ogm.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Data;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.SortedSet;

/*
    A node in the DB where we store general app level info
 */

@Data
@NodeEntity
public class AppInfo {

    @Transient
    private static final Logger log = LoggerFactory.getLogger(AppInfo.class);

    @Id @GeneratedValue Long id;
    public Long getId() {return id;}

    @Property
    private Integer schema_id;

    @Property
    private Boolean lockedDown; // set to stop anyone modifying anything

    // Count of visits to the Joseki Explorer, reported by clients asking for
    // position information.
    @Property
    public Long pageVisits;

    @Relationship("DAILY_PAGE_VISITS")
    SortedSet<DayVisitRecord> dailyPageVisits;

    public AppInfo() {
        // No-parameter constructor required as of Neo4j API 2.0.5
    };

    public void incrementVisitCount(User the_user) {
        // J01Application.debug("Incrementing visit counts...", log);

        this.pageVisits++;

        Instant now = Instant.now();

        DayVisitRecord currentRecord = this.dailyPageVisits.last();

        if (now.truncatedTo(ChronoUnit.DAYS).compareTo(currentRecord.getDate().truncatedTo(ChronoUnit.DAYS)) > 0) {
            J01Application.debug("New day for visit counts!", log);

            currentRecord = new DayVisitRecord(Instant.now());
            currentRecord.setPageVisits(1L);
            this.dailyPageVisits.add(currentRecord);
        }
        else {
            currentRecord.setPageVisits(currentRecord.getPageVisits() +1);
        }

        if (the_user.getUserId() == 0L) {
            if (currentRecord.getGuestPageVisits() == null) {
                // historical DayVisitRecord that didn't have this member
                currentRecord.setGuestPageVisits(1L);
            }
            else {
                currentRecord.setGuestPageVisits(currentRecord.getGuestPageVisits() + 1);
            }
        }

    }
} 
