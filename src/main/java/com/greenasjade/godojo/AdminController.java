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
        log.info("App Info request");

        // note: there should only be one (or zero) app_info in app_infos!
        Iterable<AppInfo> app_info = app_infos.findAll();

        if (!app_info.iterator().hasNext()) {
            // When we were on schema 0, we had no app info.
            return new AppInfoDTO();
        }
        return new AppInfoDTO(app_infos.findAll().iterator().next());
    }

}