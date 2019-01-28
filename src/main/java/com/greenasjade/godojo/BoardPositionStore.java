package com.greenasjade.godojo;

import java.util.*;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.*;
//import org.springframework.data.neo4j.annotation.Query;
//import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.query.Param;

public interface BoardPositionStore extends Neo4jRepository<BoardPosition, Long> {
	BoardPosition findByPlay(String play);
	Optional<BoardPosition> findById(Long id);
	
	@Query("MATCH (p:BoardPosition)<-[prel:PARENT]-(c:BoardPosition) WHERE id(p)={ParentID} RETURN c, prel ORDER BY c.seq")
	public List<BoardPosition>findByParentId(@Param("ParentID") Long id);	
}