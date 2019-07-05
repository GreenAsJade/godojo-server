package com.greenasjade.godojo;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import java.util.List;

public interface Tags extends Neo4jRepository<Tag, Long> {
	@Query("MATCH (t:Tag) RETURN t ORDER BY t.group, t.seq")
	List<Tag> listTags();
}