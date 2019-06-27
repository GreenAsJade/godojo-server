package com.greenasjade.godojo;

import java.util.*;

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

    // note: by definition the result of this query is an "active" position, since it has a parent.
    @Query("MATCH (p:BoardPosition)<-[prel:PARENT]-(c:BoardPosition) WHERE id(p)={ParentID} RETURN c, prel ORDER BY c.seq")
    List<BoardPosition> findByParentId(@Param("ParentID") Long id);

    // Node manipulation

    @Query("MATCH (parent:BoardPosition)<-[link:PARENT]-(target:BoardPosition) WHERE id(target)={TargetID} DELETE link")
    void removeParent(@Param("TargetID") Long id);


    // Find variations

    @Query("MATCH (p:BoardPosition)<-[:PARENT]-(n:BoardPosition)<-[:PARENT*0..]-(c:BoardPosition), " +
            "(t:Tag), (s:JosekiSource) WHERE " +
            "id(p)={TargetID} AND " +
            "({ContributorID} IS NULL or c.contributor = {ContributorID}) AND " +
            "({TagID} IS NULL or (id(t) = {TagID} AND (c)-->(t)) ) AND " +
            "({SourceID} IS NULL or (id(s) = {SourceID} AND (c)-->(s)) ) " +
            "RETURN DISTINCT n")

    List<BoardPosition> findFilteredVariations(@Param("TargetID") Long targetId,
                                               @Param("ContributorID") Long contributorId,
                                               @Param("TagID") Long tagId,
                                               @Param("SourceID") Long sourceId);


    @Query("MATCH (p:BoardPosition)<-[:PARENT]-(n:BoardPosition)<-[:PARENT*0..]-(c:BoardPosition)-->(t:Tag)  WHERE id(p)={TargetID} AND id(t)={TagID} RETURN n")
    List<BoardPosition> findVariationsToTag(@Param("TargetID") Long targetId,
                                            @Param("TagID") Long tagId);


    @Query("MATCH (p:BoardPosition)<-[:PARENT]-(n:BoardPosition)<-[:PARENT*0..]-(c:BoardPosition) where id(p)={TargetID} and c.contributor={ContributorID} RETURN n")
    List<BoardPosition> findVariationsOfContributor(@Param("TargetID") Long targetId,
                                                    @Param("ContributorID") Long contributorId);

    /* No real home for this, plonked it here... */
    // Database Utility function
    //  NOTE THAT THIS DELETES *EVERYTHING* NOT JUST BOARD POSITIONS
    @Query("MATCH (n) DETACH DELETE n")
    void deleteEverythingInDB();
}