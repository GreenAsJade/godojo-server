package com.greenasjade.godojo;

import java.util.List;

//import org.springframework.data.repository.CrudRepository;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.*;

public interface MoveStore extends Neo4jRepository<Move, Long> {
	public Move findByPlacement(String placement);
}