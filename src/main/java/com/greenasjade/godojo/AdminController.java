package com.greenasjade.godojo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.bind.annotation.*;

@RestController
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    private AppInfos app_infos;

    public AdminController(
            AppInfos app_infos) {
        this.app_infos = app_infos;
    }

    @CrossOrigin()
    @ResponseBody()
    @GetMapping("/godojo/appinfo" )
    public AppInfoDTO appInfo() {
        J01Application.debug("App Info request", log);

        AppInfo app_info = app_infos.getAppInfo();

        return new AppInfoDTO(app_info);
    }

}