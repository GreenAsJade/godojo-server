package com.greenasjade.godojo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.PagingAndSortingRepository;
import java.util.Optional;
import java.util.ArrayList;
import java.util.stream.Stream;

public interface Audits extends PagingAndSortingRepository<Audit, Long> {
    Optional<Audit> findById(Long id);

    // Audits for a node sorted in forward time order for individual node history display
    @Query("MATCH (a:Audit) -[ref:AUDIT]->(p:BoardPosition) WHERE id(p)={NodeId} RETURN a,ref,p ORDER BY a.date ASC, a.seq ASC")
    ArrayList<Audit> findByNodeId(@Param("NodeId") Long node_id);

    // Utility to find root
    @Query("MATCH (p:BoardPosition) WHERE p.play = \".root\" RETURN id(p)")
    String getRootId();

    // All audits, in reverse time order, for audit management.
    @Query(
            value="MATCH (a:Audit)-[ref:AUDIT]->(p:BoardPosition) RETURN a, ref, p ORDER BY a.date DESC, a.seq DESC",
            countQuery="MATCH (a:Audit)-[ref:AUDIT]->(p:BoardPosition) RETURN count(a)")
    Page<Audit> getAudits(Pageable pageable);

    // Audits relating to a user, for audit management
    @Query(
            value="MATCH (a:Audit)-[ref:AUDIT]->(p:BoardPosition) WHERE a.user_id={UserID} RETURN a, ref, p ORDER BY a.date DESC, a.seq DESC",
            countQuery="MATCH (a:Audit)-[ref:AUDIT]->(p:BoardPosition) WHERE a.user_id={UserID} RETURN count(a)")
    Page<Audit> getAuditsForUser(@Param("UserID") Long user_id, Pageable pageable);

    // Audits relating to a position, for audit management
    // (this is just a pageable version of findByNodeId <- that thing could be factored away at some point)
    @Query(
            value="MATCH (a:Audit)-[ref:AUDIT]->(p:BoardPosition) WHERE id(p)={PositionID} RETURN a, ref, p ORDER BY a.date DESC, a.seq DESC",
            countQuery="MATCH (a:Audit)-[ref:AUDIT]->(p:BoardPosition) WHERE id(p)={PositionID} RETURN count(a)")
    Page<Audit> getAuditsForPosition(@Param("PositionID") Long position_id, Pageable pageable);

    @Query(
            value="MATCH (a:Audit)-[ref:AUDIT]->(p:BoardPosition) WHERE a.type={Type} RETURN a, ref, p ORDER BY a.date DESC, a.seq DESC",
            countQuery="MATCH (a:Audit)-[ref:AUDIT]->(p:BoardPosition) WHERE a.type={Type} RETURN count(a)")
    Page<Audit> getAuditsOfType(@Param("Type") String audit_type, Pageable pageable);

    @Query("MATCH (a:Audit) RETURN a")
    Stream<Audit> streamAllAudits();
}