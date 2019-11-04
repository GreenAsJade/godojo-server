package com.greenasjade.godojo;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.SortedSet;

@Data
public class AppInfoDTO {
    private static final Logger log = LoggerFactory.getLogger(AppInfoDTO.class);

    private Integer schema_version;

    private Long page_visits;

    private SortedSet<DayVisitRecord> daily_visits;

    private Boolean locked_down;

    // Outgoing info

    AppInfoDTO(AppInfo appInfo) {
        schema_version = appInfo.getSchema_id();
        page_visits = appInfo.getPageVisits();
        locked_down = appInfo.getLockedDown();
        // Will need to page this, or something else suitable, in due course
        daily_visits = appInfo.getDailyPageVisits();
    }
}
