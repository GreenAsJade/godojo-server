package com.greenasjade.godojo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@RestController
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private Users user_access;
    private UserFactory user_factory;
    private BoardPositions bp_access;
    public UserController(
            Users user_access,
            UserFactory user_factory,
            BoardPositionsNative bpn_access) {
        this.user_access = user_access;
        this.user_factory = user_factory;
        this.bp_access = new BoardPositions(bpn_access);
    }

    @CrossOrigin()
    @ResponseBody()
    @GetMapping("/godojo/contributors" )
    public List<Long> contributors(
            @RequestHeader("X-User-Info") String user_jwt) {
        return user_access.findContributors();
    }

    /* This one is for getting the permissions of the browser user */
    @CrossOrigin()
    @ResponseBody()
    @GetMapping("/godojo/user-permissions" )  // user id param not needed because we have user jwt anyhow
    public PermissionsDTO userPermissions(
            @RequestHeader("X-User-Info") String user_jwt) {

        User the_user = this.user_factory.createUser(user_jwt);

        return new PermissionsDTO(the_user);
    }

    /* This one is for getting the permissions of any user by id */
    @CrossOrigin()
    @ResponseBody()
    @GetMapping("/godojo/permissions" )
    public PermissionsDTO permissions(
            @RequestParam(value="id") Long id) {

        User the_user = this.user_factory.createUser(id);

        return new PermissionsDTO(the_user);
    }

    @CrossOrigin()
    @ResponseBody()
    @PutMapping("/godojo/permissions")
    // Update user permissions
    public PermissionsDTO updatePermissions(
            @RequestHeader("X-User-Info") String user_jwt,
            @RequestParam(value="id") Long id,
            @RequestBody PermissionsDTO permissions) {

        J01Application.debug("set permissions request " + permissions, log);

        User the_user = this.user_factory.createUser(user_jwt);
        Long user_id = the_user.getUserId();

        if (!the_user.isAdministrator()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, String.format("User %s does not have admin permissions", user_id.toString())
            );
        }

        User target_user = this.user_factory.createUser(id);

        target_user.setCanComment(permissions.getCan_comment());
        target_user.setCanEdit((permissions.getCan_edit()));
        target_user.setAdministrator(permissions.getIs_admin());

        user_access.save(target_user);

        return new PermissionsDTO(target_user);
    }

    @CrossOrigin()
    @ResponseBody()
    @GetMapping("/godojo/playrecord" )  // user id param not needed because we have user jwt anyhow
    public PlayRecordDTO userPlayRecord(
            @RequestHeader("X-User-Info") String user_jwt) {

        User the_user = this.user_factory.createUser(user_jwt);

        return new PlayRecordDTO(the_user);
    }

    @CrossOrigin()
    @ResponseBody()
    @PutMapping("/godojo/playrecord")
    // Update position play record
    public PlayRecordDTO updatePlayRecord(
            @RequestHeader("X-User-Info") String user_jwt,
            @RequestBody PlayRecordDTO data) {

        J01Application.debug("set play record request " + data, log);

        User the_user = this.user_factory.createUser(user_jwt);

        // see if the user already played this position before
        PlayRecord played = the_user.played_josekis.stream()
                .filter(p -> p.getPosition().id.equals(data.getPosition_id()))
                .findFirst()
                .orElse(null);

        if (played == null) {
            J01Application.debug("new play record needed", log);
            BoardPosition target = bp_access.findById(data.getPosition_id());
            played = new PlayRecord(the_user, target);
            the_user.played_josekis.add(played);
        }

        played.setAttempts(played.getAttempts() + 1);

        if (played.getBest_attempt() == -1 || data.getError_count() < played.getBest_attempt()) {
            played.setBest_attempt(data.getError_count());
        }

        if (data.getError_count() == 0) {
            played.setSuccesses(played.getSuccesses() + 1);
        }

        user_access.save(the_user);
        return new PlayRecordDTO(played);
    }
}