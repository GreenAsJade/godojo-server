package com.greenasjade.godojo;

import java.util.*;

import java.util.stream.Stream;

import org.springframework.data.neo4j.annotation.Query;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

// This "native" interface is shimed for use by BoardPositions due to
// https://stackoverflow.com/q/55964038/554807
// Logic that should be in custom queries but can't be is in the BoardPositions shim.

public interface BoardPositionsNative extends PagingAndSortingRepository<BoardPosition, Long> {

    // NOTE:  These find _all_ nodes, including inactive ("deleted") ones
    // Inactive nodes have no parent.   Except "root" which is defined to be active.

    List<BoardPosition> findByPlay(String play);  // There can be more than one due to old deactivated ones.
    Optional<BoardPosition> findById(Long id);

    List<BoardPosition> findAll();

    // note: by definition the result of this query is an "active" position, since it has a parent.
    @Query("MATCH (p:BoardPosition)<-[prel:PARENT]-(c:BoardPosition) WHERE id(p)={ParentID} RETURN c, prel ORDER BY c.seq")
    List<BoardPosition> findByParentId(@Param("ParentID") Long id);

    // Node manipulation

    @Query("MATCH (parent:BoardPosition)<-[link:PARENT]-(target:BoardPosition) WHERE id(target)={TargetID} DELETE link")
    void removeParent(@Param("TargetID") Long id);

    // Find variations filtered by Tag and optionally source and contributor

    @Query("MATCH (t:Tag)<-[:TAGS]-(leaf:BoardPosition)-[:PARENT*0..]->(v:BoardPosition)-[:PARENT]->(target:BoardPosition) " +
            "WHERE id(target) = {TargetID} " +
            "WITH v, leaf, collect(id(t)) as tids " +
            "WHERE ALL (tid in {TagIDs} WHERE tid in tids) " +
            "WITH v, leaf WHERE " +
            "({ContributorID} IS NULL OR leaf.contributor = {ContributorID}) " +
            "WITH v,leaf MATCH (s:JosekiSource) " +
            "WHERE ({SourceID} IS NULL OR (id(s) = {SourceID} AND (leaf)-[:SOURCE]->(s)) ) " +
            "RETURN DISTINCT v")
    List<BoardPosition> findFilteredVariations(@Param("TargetID") Long targetId,
                                               @Param("ContributorID") Long contributorId,
                                               @Param("TagIDs") List<Long> tagIds,
                                               @Param("SourceID") Long sourceId);


    @Query("MATCH (p:BoardPosition)<-[:PARENT*1..]-(c:BoardPosition) where id(p)={TargetID} return count(c)")
    Integer countChildren(@Param("TargetID") Long id);

    @Query("MATCH (p:BoardPosition)<-[:PARENT*1..]-(c:BoardPosition)-[:TAGS]->(t:Tag) where id(p)={TargetID} and id(t)={TagID} return count(c)")
    Integer countChildrenWithTag(@Param("TargetID") Long id, @Param("TagID") Long tagId);

    @Query("MATCH (p:BoardPosition) WHERE p.variation_label = '0' RETURN p")
    Stream<BoardPosition> findVariationLabelZeros();

    /* No real home for this, plonked it here... */
    // Database Utility function
    // Delete 'Limit' nodes (at random), return how many.
    // This needs to be called until none are left to delete everything
    //  NOTE THAT THIS DELETES *EVERYTHING* NOT JUST BOARD POSITIONS
    @Query("MATCH (n) WITH n LIMIT {Limit} DETACH DELETE n RETURN count(*)")
    int deleteNodes(@Param("Limit") int Limit);
}