package com.greenasjade.godojo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    private AppInfos app_infos;

    private UserFactory user_factory;

    public AdminController(
            AppInfos app_infos,
            UserFactory user_factory) {
        this.app_infos = app_infos;
        this.user_factory = user_factory;
    }

    @CrossOrigin()
    @ResponseBody()
    @GetMapping("/godojo/appinfo" )
    public AppInfoDTO appInfo() {
        J01Application.debug("App Info request", log);

        AppInfo app_info = app_infos.getAppInfo();

        return new AppInfoDTO(app_info);
    }

    @CrossOrigin()
    @ResponseBody()
    @GetMapping("/godojo/lockdown" )
    public Boolean getLockdown() {
        J01Application.debug("Lockdown status request", log);

        AppInfo app_info = app_infos.getAppInfo();

        return app_info.getLockedDown();
    }

    @CrossOrigin()
    @ResponseBody()
    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/godojo/lockdown")
    // Update lockdown setting
    public String setLockdown(
            @RequestHeader("X-User-Info") String user_jwt,
            @RequestParam(value="lockdown") Boolean lockdown) {

        J01Application.debug("set lockdown " + lockdown.toString(), log);

        User the_user = this.user_factory.createUser(user_jwt);
        Long user_id = the_user.getUserId();

        if (!the_user.isAdministrator()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, String.format("User %s does not have admin permissions for lockdown", user_id.toString())
            );
        }

        AppInfo app_info = app_infos.getAppInfo();

        app_info.setLockedDown(lockdown);
        app_infos.save(app_info);

        return "Lockdown set " + lockdown.toString();
    }
}