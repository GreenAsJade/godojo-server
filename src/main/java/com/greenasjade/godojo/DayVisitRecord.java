package com.greenasjade.godojo;

import lombok.Data;
import org.neo4j.ogm.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;

/*

 */

@Data
@NodeEntity
public class DayVisitRecord implements Comparable<DayVisitRecord> {

    @Transient
    private static final Logger log = LoggerFactory.getLogger(DayVisitRecord.class);

    @Id @GeneratedValue Long id;

    @Property
    private Long pageVisits;

    @Property
    public Long explorePageVisits;

    @Property
    public Long playPageVisits;

    @Property
    public Long guestPageVisits;

    @Property
    private Instant date;

    public DayVisitRecord() {
        // Empty parameter constructor required as of Neo4j API 2.0.5
    };

    public DayVisitRecord(Instant date) {
        this.date = date;
        this.pageVisits = 0L;
        this.explorePageVisits = 0L;
        this.playPageVisits = 0L;
        this.guestPageVisits = 0L;
    }

    @Override
    public int compareTo(DayVisitRecord dayVisitRecord) {
        return this.date.compareTo(dayVisitRecord.getDate());
    }
}
