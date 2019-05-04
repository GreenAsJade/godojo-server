package com.greenasjade.godojo;

import java.util.List;

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

    // NOTE:  These find _all_ nodes, including inactive ("deleted") ones
    // Inactive nodes have no parent.   Except "root" which is defined to be active.
    BoardPosition findByPlay(String play) { return bp_access.findByPlay(play);}
    BoardPosition findById(Long id) { return bp_access.findById(id).orElse(null); }

    // These methods are for working with active nodes.
    // An active node must have a parent (or is root)

    BoardPosition findActiveByPlay(String play) {
        BoardPosition target = bp_access.findByPlay(play);
        return (target != null && (target.parent != null || target.getPlay().equals(".root"))) ? target : null;
    }

    // Note that the results here are by definition active
    List<BoardPosition> findByParentId(Long id) { return bp_access.findByParentId(id); }

    // Database Utility function
    //  NOTE THAT THIS DELETES *EVERYTHING* NOT JUST BOARD POSITIONS
    void deleteEverythingInDB() { bp_access.deleteEverythingInDB(); }

}
