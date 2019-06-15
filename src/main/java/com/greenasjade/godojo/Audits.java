package com.greenasjade.godojo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.PagingAndSortingRepository;
import java.util.Optional;
import java.util.ArrayList;

public interface Audits extends PagingAndSortingRepository<Audit, Long> {
    Optional<Audit> findById(Long id);

    // Audits for a node sorted in forward time order for individual node history display
    @Query("MATCH (a:Audit) -[ref:AUDIT]->(p:BoardPosition) WHERE id(p)={node_id} RETURN a,ref,p ORDER BY a.seq ASC")
    ArrayList<Audit> findByNodeId(@Param("node_id") Long node_id);

    // Utility to find root
    @Query("MATCH (p:BoardPosition) WHERE p.play = \".root\" RETURN id(p)")
    String getRootId();

    // All audits, in reverse time order, for audit management.
    @Query(
            value="MATCH (a:Audit)-[ref:AUDIT]->(p:BoardPosition) RETURN a, ref, p ORDER BY a.date DESC, a.seq DESC",
            countQuery="MATCH (a:Audit)-[ref:AUDIT]->(p:BoardPosition) RETURN count(a)")
    Page<Audit> getAudits(Pageable pageable);
}