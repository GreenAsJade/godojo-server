package com.greenasjade.godojo;

import java.util.*;

import org.springframework.data.neo4j.repository.*;
//import org.springframework.data.neo4j.annotation.Query;
//import org.springframework.data.repository.query.Param;

public interface BoardPositionStore extends Neo4jRepository<BoardPosition, Long> {
	BoardPosition findByPlay(String play);
	Optional<BoardPosition> findById(Long id);
}