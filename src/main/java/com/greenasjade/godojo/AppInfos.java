package com.greenasjade.godojo;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;

//  Neo4j interface for AppInfo

public interface AppInfos extends Neo4jRepository<AppInfo, Long> {
    @Query("MATCH (i:AppInfo) RETURN i LIMIT 1")
    AppInfo getAppInfo();

}
