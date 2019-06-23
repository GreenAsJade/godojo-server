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

    public UserController(
            Users user_access,
            UserFactory user_factory) {
        this.user_access = user_access;
        this.user_factory = user_factory;
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

        log.info("set permissions request " + permissions);

        User the_user = this.user_factory.createUser(user_jwt);
        Long user_id = the_user.getUserId();

        if (!the_user.isAdministrator()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, String.format("User %s does not have admin permissions", user_id.toString())
            );
        }

        User target_user = this.user_factory.createUser(id);

        user_factory.updatePermissions(target_user, permissions);

        return new PermissionsDTO(target_user);
    }

}