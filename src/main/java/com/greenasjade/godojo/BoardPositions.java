package com.greenasjade.godojo;

import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.stream.Collectors;

// This should be a native BoardPosition Repository interface
// This nasty shim to the actual interface is because of
// https://stackoverflow.com/q/55964038/554807
// This shim layer implements logic that would ideally be native in custom queries.

public class BoardPositions {

    private BoardPositionsNative bp_access;

    public BoardPositions(
            BoardPositionsNative bp_access) {
        this.bp_access = bp_access;
    }

    void save(BoardPosition position) { bp_access.save(position); }

    // NOTE:  This finds _all_ nodes, including inactive ("deleted") ones
    // Inactive nodes have no parent.   Except "root" which is defined to be active.

    BoardPosition findById(Long id) { return bp_access.findById(id).orElse(null); }

    // These methods are for working with active nodes.
    // An active node must have a parent (or is root)

    BoardPosition findActiveByPlay(String play) {
        List<BoardPosition> candidates = bp_access.findByPlay(play);

        candidates = candidates.stream()
                .filter(
                        candidate -> candidate.parent != null || candidate.getPlacement().equals("root"))
                .collect(Collectors.toList());

        if (candidates.size() > 1) {
            throw(new RuntimeException("More than one active node for play " + play));
        }

        return (candidates.size() == 0 ? null : candidates.get(0));
    }

    // Note that the results here are by definition active
    List<BoardPosition> findByParentId(Long id) { return bp_access.findByParentId(id); }


    void removeParent(Long id) {  bp_access.removeParent(id); }

    // Database Utility function
    //  NOTE THAT THIS DELETES *EVERYTHING* NOT JUST BOARD POSITIONS
    void deleteEverythingInDB() { bp_access.deleteEverythingInDB(); }

}
