package com.greenasjade.godojo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class AuditsController {

    private static final Logger log = LoggerFactory.getLogger(AuditsController.class);

    private Audits audit_store;
    private BoardPositions bp_access;

    private UserFactory user_factory;

    public AuditsController(
            Audits audit_store,
            BoardPositionsNative bp_access,
            UserFactory user_factory) {
        this.audit_store = audit_store;
        this.bp_access = new BoardPositions(bp_access);
        this.user_factory = user_factory;
    }

    @CrossOrigin()
    @ResponseBody()
    @GetMapping("/godojo/audits" )
    // Return all the information needed to display audit log for a single position
    // note: surely this should have been a Page<AuditDTO>.  I think this was here before I knew about that...
    public ArrayList<Audit> audits(
            @RequestParam(value = "id") String node_id) {

        if (node_id.equals("root")) {
            node_id = audit_store.getRootId();
        }
        J01Application.debug("Audit request for: " + node_id, log);

        return audit_store.findByNodeId(Long.parseLong(node_id));
    }

    @CrossOrigin()
    @ResponseBody()
    @GetMapping("/godojo/changes" )
    // Return recent change information
    public Page<AuditDTO> changes(
            @RequestParam(value = "position_id", required = false, defaultValue = "-1") Long position_id,
            @RequestParam(value = "user_id", required = false, defaultValue = "-1") Long user_id,
            @RequestParam(value = "audit_type", required = false, defaultValue = "") String audit_type,
            Pageable pageable) {

        J01Application.debug(String.format("Change log request: %d, %d, '%s'", position_id, user_id, audit_type), log);

        if (position_id != -1L) {
            return audit_store.getAuditsForPosition(position_id, pageable).map(audit -> new AuditDTO(audit));
        }
        else if (user_id != -1L) {
            return audit_store.getAuditsForUser(user_id, pageable).map(audit -> new AuditDTO(audit));
        }
        else if (!audit_type.equals("")) {
            return audit_store.getAuditsOfType(audit_type, pageable).map(audit -> new AuditDTO(audit));
        }
        else {
            return audit_store.getAudits(pageable).map(audit -> new AuditDTO(audit));
        }
    }

    private String RevertCategoryChange(Audit target, Long user_id) {
        if (target.ref.getCategory().toString().equals(target.getNewValue())) {
            J01Application.debug("proceeding with revert on " + target.ref, log);
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
            J01Application.debug("proceeding with revert on " + target.ref, log);
            target.ref.setDescription(target.getOriginalValue(), user_id);
            this.bp_access.save(target.ref);
            return "done.";
        }
        else {
            return "not done: this was not the most recent change to the description of this position.";
        }
    }

    private void DeleteBoardPosition(BoardPosition target, Long user_id) {
        J01Application.debug("'DELETING' " + target.getPlacement(), log);

        BoardPosition losing_parent = target.parent;

        bp_access.removeParent(target.id);

        // We audit the parent first, so that when reversing the sequence the child DEACTIVATE is the one that is done first,
        // so we don't have to reverse a REMOVE_CHILD.  Reversing child DEACTIVATE is easier.

        // To be honest, I don't understand why we don't have to explicitly load these,
        // so it won't surprise me if at some point they appear empty and needing loading here.
        J01Application.debug("losing parent audits: " + losing_parent + " "  + losing_parent.id + " " + losing_parent.audits, log);

        losing_parent.audits.add(new Audit(losing_parent, ChangeType.REMOVE_CHILD, target.id.toString(), "", "Removed child " + target.getPlacement(), user_id));
        bp_access.save(losing_parent);

        target.audits.add(new Audit(target, ChangeType.DEACTIVATE, losing_parent.id.toString(),"", "Removed (deactivated)", user_id));
        bp_access.save(target);
    }

    private String RemoveAddedPosition(BoardPosition target_child, BoardPosition from, Long created_by_id, Long user_id) {
        J01Application.debug("RemoveAddedPosition: " + target_child.getPlacement() + " from " + from, log);
        J01Application.debug("initial children: " + from.children, log);

        if (target_child.children != null) {
            // Take a copy of the children list, so we can remove children from the actual children list as we go.
            List<BoardPosition> targets_children = target_child.children;
            for (BoardPosition grand_child : targets_children) {

                grand_child = bp_access.findById(grand_child.id); // get the new child fully loaded from neo

                String grand_child_removal_result = RemoveAddedPosition(grand_child, target_child, created_by_id, user_id);

                if (!grand_child_removal_result.equals("done.")) {
                    // couldn't remove the grand child, so we can't proceed to remove this node
                    return grand_child_removal_result;
                }
            }
        }
        else {
            J01Application.debug("(no children)", log);
        }
        if (target_child.getContributorId().equals(created_by_id)) {
            DeleteBoardPosition(target_child, user_id);
            return "done.";
        }
        else {
            return "not reverting addition of "+ target_child.getPlacement() +
                    " because it's creator " + target_child.getContributorId().toString() + " doesn't match " + created_by_id.toString();
        }
    }

    private String RevertAddChild(Audit target, Long user_id) {
        J01Application.debug("Add Child Reversion request for " + target.ref.getPlacement(), log);
        J01Application.debug("target child " + target.getNewValue(), log);
        J01Application.debug("created by " + target.getUserId().toString(), log);

        // We need the target and it's parent fully loaded from neo (for audit etc)

        BoardPosition target_child = bp_access.findActiveByPlay(target.getNewValue());

        if (target_child == null) {
            return "not done.  It looks like this child is already removed.";
        }

        if (target_child.getPlay().equals(".root")) {
            return "You can't undo the empty board, silly!";
        }

        BoardPosition targets_parent = bp_access.findById(target_child.parent.id);

        return RemoveAddedPosition(target_child, targets_parent, target.getUserId(), user_id);
    }

    private String RevertDeactivate(Audit target, Long user_id) {
        Long target_parent_id = Long.parseLong(target.getOriginalValue());
        BoardPosition target_parent = bp_access.findById(target_parent_id);

        if (target_parent.parent == null) {
            return "can't re-activate " + target.ref.getPlacement() + " on to " + target_parent + "because that node is not active";
        }

        target.ref.parent = target_parent;
        target.ref.audits.add(new Audit(target.ref, ChangeType.REACTIVATE, "Reactivated onto " + target_parent.getPlay(), user_id));
        target_parent.audits.add(new Audit(target_parent, ChangeType.REACTIVATE, "Reactivated child " + target.ref.getPlacement(), user_id));

        bp_access.save(target.ref);

        return "done.";
    }

    private String CheckNodeCreationReversionDone(Audit target) {
        // When we are asked to revert a "CREATE" audit, we simply check that the
        // previous required "ADD_CHILD" reversion was done, which will have taken care of removing the node

        J01Application.debug("Checking add child reversal for " + target, log);

        if (target.ref.parent == null) {
            return "checks OK: the node does not exist or is not active";
        }
        else {
            return "not done: the node still is still active, you need to revert the 'ADD_CHILD' that added this node";
        }
    }

    private String CheckDeactivateReversionDone(Audit target) {
        J01Application.debug("Checking deactivation reversal done for target", log);

        if (bp_access.findById(Long.parseLong(target.getOriginalValue())).parent != null) {
            return "checks OK: the child node is active";
        }
        else {
            return "not done: the child node seems to be still inactive, you need to revert the DEACTIVATE";
        }
    }

    private String RevertAddComment(Audit target, Long user_id) {
        String target_comment_date = target.getOriginalValue();

        J01Application.debug(String.format("Reverting comment made at %s from %s", target_comment_date, target.ref.getInfo()), log);

        int target_comment_index = IntStream.range(0, target.ref.commentary.size())
                .filter(i -> target_comment_date.equals(target.ref.commentary.get(i).getDate().toString()))
                .findFirst()
                .orElse(-1);

        Comment target_comment = target.ref.commentary.get(target_comment_index);

        Long target_comment_id = target_comment.id;
        String target_comment_remark = target_comment.getComment();

        target.ref.commentary.remove(target_comment_index);

        J01Application.debug(target.ref.commentary.toString(), log);

        target.ref.audits.add(new Audit(target.ref, ChangeType.REMOVE_COMMENT,
                        target_comment_id.toString(), target_comment_remark,
                        "Removed comment", user_id));

        bp_access.save(target.ref);
        return "done.";
    }

    @Transactional
    @CrossOrigin()
    @ResponseBody()
    @PostMapping("/godojo/revert")
    RevertResultDTO revert(
            @RequestHeader("X-User-Info") String user_jwt,
            @RequestBody RevertRequestDTO request) {
        log.info("revert request " + request);

        User the_user = this.user_factory.createUser(user_jwt);

        Long user_id = the_user.getUserId();

        if (!the_user.isAdministrator()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, String.format("User %s does not have admin permissions", user_id.toString())
            );
        }

        Audit target_audit = audit_store.findById(request.audit_id).orElse(null);

        if (target_audit == null) {
            return new RevertResultDTO("invalid audit!");
        }

        J01Application.debug("which is " + target_audit + " pointing to " + target_audit.ref, log);

        // load all the child info of the board position (we need the audits array)
        target_audit.ref = bp_access.findById(target_audit.ref.id);

        J01Application.debug("After load, the target node has children " + target_audit.ref.children, log);

        String result;

        switch (target_audit.getType()) {
            case CREATED: result = CheckNodeCreationReversionDone(target_audit); break;
            case ADD_CHILD: result = RevertAddChild(target_audit, user_id); break;
            case CATEGORY_CHANGE: result = RevertCategoryChange(target_audit, user_id); break;
            case DESCRIPTION_CHANGE: result = RevertDescriptionChange(target_audit, user_id); break;
            case DEACTIVATE: result = RevertDeactivate(target_audit, user_id); break;
            case REMOVE_CHILD: result = CheckDeactivateReversionDone(target_audit); break;
            case ADD_COMMENT: result = RevertAddComment(target_audit, user_id); break;
            case REACTIVATE: result = "not done, just redo the deactivate instead!"; break;
            case SOURCE_CHANGE: result = "not done: reverting source change not supported"; break;
            default: result = "not done: unrecognised audit type in reversion request";
        }

        return new RevertResultDTO(result);
    }

}