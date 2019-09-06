package com.greenasjade.godojo;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.stream.Stream;

public interface Users extends Neo4jRepository<User, Long> {
	@Query("MATCH (u:User {user_id:{userId}}) RETURN u")
	User findByUserId(@Param("userId") Long user_id);

	@Query("MATCH (b:BoardPosition) RETURN DISTINCT b.contributor")
	List<Long> findContributors();

	@Query("MATCH (u:User) RETURN u")
	Stream<User>streamAllUsers();
}