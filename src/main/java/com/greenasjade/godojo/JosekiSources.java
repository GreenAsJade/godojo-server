package com.greenasjade.godojo;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface JosekiSources extends Neo4jRepository<JosekiSource, Long> {
	@Query("MATCH (s:JosekiSource) RETURN s")
	List<JosekiSource> listSources();
}