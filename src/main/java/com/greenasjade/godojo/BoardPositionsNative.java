package com.greenasjade.godojo;

import java.util.*;

import org.springframework.data.neo4j.annotation.Query;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

// This "native" interface is shimed for use by BoardPositions due to
// https://stackoverflow.com/q/55964038/554807
// Logic that should be in custom queries but can't be is in BoardPositions

public interface BoardPositionsNative extends PagingAndSortingRepository<BoardPosition, Long> {

    // NOTE:  These find _all_ nodes, including inactive ("deleted") ones
    // Inactive nodes have no parent.   Except "root" which is defined to be active.
    BoardPosition findByPlay(String play);
    Optional<BoardPosition> findById(Long id);

    // note: by definition the result is "active", since it has a parent.
    @Query("MATCH (p:BoardPosition)<-[prel:PARENT]-(c:BoardPosition) WHERE id(p)={ParentID} RETURN c, prel ORDER BY c.seq")
    List<BoardPosition> findByParentId(@Param("ParentID") Long id);

    // Database Utility function
    //  NOTE THAT THIS DELETES *EVERYTHING* NOT JUST BOARD POSITIONS
    @Query("MATCH (n) DETACH DELETE n")
    void deleteEverythingInDB();
}