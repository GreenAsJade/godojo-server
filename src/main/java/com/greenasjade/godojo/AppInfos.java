package com.greenasjade.godojo;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;

//  Neo4j interface for AppInfo

public interface AppInfos extends Neo4jRepository<AppInfo, Long> {
    @Query("MATCH (i:AppInfo) OPTIONAL MATCH (i)-[l]-(r:DayVisitRecord) RETURN i,l,r")
    AppInfo getAppInfo();

}
