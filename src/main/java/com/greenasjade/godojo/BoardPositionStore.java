package com.greenasjade.godojo;

import java.util.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.*;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

public interface BoardPositionStore extends PagingAndSortingRepository<BoardPosition, Long> {
    BoardPosition findByPlay(String play);
    Optional<BoardPosition> findById(Long id);

    @Query("MATCH (p:BoardPosition)<-[prel:PARENT]-(c:BoardPosition) WHERE id(p)={ParentID} RETURN c, prel ORDER BY c.seq")
    List<BoardPosition> findByParentId(@Param("ParentID") Long id);

    @Query("MATCH (n) DETACH DELETE n")
    void deleteEverythingInDB();  //  NOTE THAT THIS DELETES *EVERYTHING* NOT JUST BOARD POSITIONS

    @Query(
            value="MATCH (a:Audit)-[ref:AUDIT]->(p:BoardPosition) RETURN a, ref, p",
            countQuery="MATCH (a:Audit)-[ref:AUDIT]->(p:BoardPosition) RETURN count(a)")
    Page<Audit> getAudits(Pageable pageable);

}