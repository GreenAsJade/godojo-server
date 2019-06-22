package com.greenasjade.godojo;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import java.util.List;

// (TBH I can't recall why I don't just use built-in findAll() )

public interface Tags extends Neo4jRepository<Tag, Long> {
	@Query("MATCH (t:Tag) RETURN t")
	List<Tag> listTags();
}