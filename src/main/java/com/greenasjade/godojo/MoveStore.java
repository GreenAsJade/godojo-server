package com.greenasjade.godojo;

import java.util.List;

import org.springframework.data.neo4j.repository.*;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.repository.query.Param;

public interface MoveStore extends Neo4jRepository<Move, Long> {
	public Move findByPlacement(String placement);
	@Query("MATCH (n:Move)-[rel:CHILD]-(c:BoardPosition) RETURN n, rel, c")
	public List<Move>findAll();
	
	@Query("MATCH (p:BoardPosition)-[prel:PARENT]-(m:Move)-[crel:CHILD]-(c:BoardPosition) WHERE id(p)={ParentID} RETURN m, crel, c ORDER BY m.seq")
	public List<Move>findByParentId(@Param("ParentID") Long id);
			
}