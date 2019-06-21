package com.greenasjade.godojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Data
public class AppInfoDTO {
    private static final Logger log = LoggerFactory.getLogger(AppInfoDTO.class);

    private Integer schema_version;

    // Outgoing info

    AppInfoDTO(AppInfo appInfo) {
        schema_version = appInfo.getSchema_id();
    }

    AppInfoDTO() {
        schema_version = 0;
    }
}
