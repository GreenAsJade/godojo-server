package com.greenasjade.godojo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private UserFactory user_factory;

    public UserController(
            UserFactory user_factory) {
        this.user_factory = user_factory;
    }

    @CrossOrigin()
    @ResponseBody()
    @GetMapping("/godojo/permissions" )
    // Return all the information needed to display audit log for a single position
    public PermissionsDTO permissions(
            @RequestHeader("X-User-Info") String user_jwt) {

        User the_user = this.user_factory.createUser(user_jwt);

        return new PermissionsDTO(the_user);
    }
}