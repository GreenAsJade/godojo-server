package com.greenasjade.godojo;

import java.util.*;

import org.springframework.data.neo4j.annotation.Query;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

public interface BoardPositionStore extends PagingAndSortingRepository<BoardPosition, Long> {
    BoardPosition findByPlay(String play);
    Optional<BoardPosition> findById(Long id);

    @Query("MATCH (p:BoardPosition)<-[prel:PARENT]-(c:BoardPosition) WHERE id(p)={ParentID} RETURN c, prel ORDER BY c.seq")
    List<BoardPosition> findByParentId(@Param("ParentID") Long id);

    // Utility function
    @Query("MATCH (n) DETACH DELETE n")
    void deleteEverythingInDB();  //  NOTE THAT THIS DELETES *EVERYTHING* NOT JUST BOARD POSITIONS
}