package com.greenasjade.godojo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;

import java.util.ArrayList;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuditsController {

    private static final Logger log = LoggerFactory.getLogger(AuditsController.class);

    private Audits audit_store;
    private BoardPositions bp_access;

    public AuditsController(
            Audits audit_store,
            BoardPositionsNative bp_access) {
        this.audit_store = audit_store;
        this.bp_access = new BoardPositions(bp_access);
    }

    @CrossOrigin()
    @ResponseBody()
    @GetMapping("/godojo/audits" )
    // Return all the information needed to display audit log for a single position
    public ArrayList<Audit> audits(
            @RequestParam(value = "id", required = false, defaultValue = "root") String node_id) {

        if (node_id.equals("root")) {
            node_id = audit_store.getRootId();
        }
        log.info("Audit request for: " + node_id);

        return audit_store.findByNodeId(Long.parseLong(node_id));
    }

    @CrossOrigin()
    @ResponseBody()
    @GetMapping("/godojo/changes" )
    // Return recent change information
    public Page<AuditDTO> changes(Pageable pageable) {

        log.info("Change log request");

        return audit_store.getAudits(pageable).map(audit -> new AuditDTO(audit));
    }

    private String RevertCategoryChange(Audit target, Long user_id) {
        if (target.ref.getCategory().toString().equals(target.getNewValue())) {
            log.info("proceeding with revert on " + target.ref);
            target.ref.setCategory(PlayCategory.valueOf(target.getOriginalValue()), user_id);
            this.bp_access.save(target.ref);
            return "done.";
        }
        else {
            return "not done: this was not the most recent change to the category of this position.";
        }
    }

    private String RevertDescriptionChange(Audit target, Long user_id) {
        if (target.ref.getDescription().equals(target.getNewValue())) {
            log.info("proceeding with revert on " + target.ref);
            target.ref.setDescription(target.getOriginalValue(), user_id);
            this.bp_access.save(target.ref);
            return "done.";
        }
        else {
            return "not done: this was not the most recent change to the description of this position.";
        }
    }

    void DeleteBoardPosition(BoardPosition target) {
        log.info("DELETE " + target.getPlacement());
    }

    private String RemoveAddedPosition(BoardPosition target_child, Long created_by_id) {
        log.info("RemoveAddedPosition: " + target_child.getPlacement());
        if (target_child.children != null) {
            for (BoardPosition grand_child : target_child.children) {
                // get the new child fully loaded from neo
                grand_child = bp_access.findById(grand_child.id);
                String grand_child_removal_result = RemoveAddedPosition(grand_child, created_by_id);
                if (!grand_child_removal_result.equals("done.")) {
                    // couldn't remove the grand child, so we can't proceed to remove this node
                    return grand_child_removal_result;
                }
            }
        }
        else {
            log.info("(no children)");
        }
        if (target_child.getContributorId().equals(created_by_id)) {
            DeleteBoardPosition(target_child);
            return "done.";
        }
        else {
            return "not reverting addition of "+ target_child.getPlacement() +
                    " because it's creator " + target_child.getContributorId().toString() + " doesn't match " + created_by_id.toString();
        }
    }

    private String RevertAddChild(Audit target, Long user_id) {
        log.info("Add Child Reversion request for " + target.ref.getPlacement());
        log.info("target child " + target.getNewValue());
        log.info("created by " + target.getUserId().toString());

        BoardPosition target_child = bp_access.findActiveByPlay(target.getNewValue());

        return RemoveAddedPosition(target_child, target.getUserId());
    }

    private String CheckAddChildReversionDone(Audit target) {
        // When we are asked to revert a "CREATE" audit, we simply check that the
        // previous required "ADD_CHILD" reversion was done, which will have taken care of removing the node

        if (target.ref == null) {
            return "checks OK: the node does not exist";
        }
        else {
            return "not done: the node still exists, you need to revert the 'ADD_CHILD' for this node";
        }
    }

    @CrossOrigin()
    @ResponseBody()
    @PostMapping("/godojo/revert")
    RevertResultDTO revert(
            @RequestHeader("X-User-Info") String user_jwt,
            @RequestBody RevertRequestDTO request) {
        log.info("revert request " + request);

        User the_user = new User(user_jwt);

        Long user_id = the_user.getId();

        /* TBD make sure user is authorised before proceeding.
         */

        Audit target_audit = audit_store.findById(request.audit_id).orElse(null);

        if (target_audit == null) {
            return new RevertResultDTO("invalid audit!");
        }

        log.info("which is " + target_audit + " pointing to " + target_audit.ref);

        // load all the child info of the board position (we need the audits array)
        target_audit.ref = bp_access.findById(target_audit.ref.id);

        log.info("After load, the target node has children " + target_audit.ref.children);

        String result;

        switch (target_audit.getType()) {
            case CREATED: result = CheckAddChildReversionDone(target_audit); break;
            case ADD_CHILD: result = RevertAddChild(target_audit, user_id); break;
            case CATEGORY_CHANGE: result = RevertCategoryChange(target_audit, user_id); break;
            case DESCRIPTION_CHANGE: result = RevertDescriptionChange(target_audit, user_id); break;
            case SOURCE_CHANGE: result = "not done: reverting source change not supported"; break;
            default: result = "not done: unrecognised audit type in reversion request";
        }

        return new RevertResultDTO(result);
    }

}