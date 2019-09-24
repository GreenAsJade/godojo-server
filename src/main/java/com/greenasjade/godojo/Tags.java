package com.greenasjade.godojo;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface Tags extends Neo4jRepository<Tag, Long> {
    @Query("MATCH (t:Tag) RETURN t ORDER BY t.group, t.seq")
    List<Tag> listTags();

    // Although we are looking for one (only) tag,
    // this returns a list of one or zero elements, for ease
    // of inserting into a TagsDTO
    @Query("MATCH (t:Tag {group: {Group}, seq: {Seq}}) RETURN t")
    List<Tag> findTagByGroupSeq(@Param("Group") Integer group, @Param("Seq") Integer seq);

}