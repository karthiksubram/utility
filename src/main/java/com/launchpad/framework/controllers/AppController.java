package com.launchpad.framework.controllers;

import com.launchpad.framework.dto.AppMetadata;
import com.launchpad.framework.service.AppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/apps")
public class AppController {
    @Autowired
    private AppService appService;

    @PostMapping("/register")
    public ResponseEntity<String> registerApp(@RequestParam("file") MultipartFile file,
                                              @RequestParam("name") String name,
                                              @RequestParam("description") String description) {
        return ResponseEntity.ok(appService.registerApp(name, description, file));
    }

    @GetMapping("/list")
    public List<AppMetadata> listApps() {

        return appService.getAllApps();

    }


    @GetMapping("/download/{appId}")
    public ResponseEntity<Resource> downloadApp(@PathVariable Long appId) {
        return appService.getAppFile(appId);
    }

    @GetMapping("/execute/{appId}")
    public String executeApp(@PathVariable Long appId) {
        return appService.executeApp(appId);
    }

    @PostMapping("/stop/{pid}")
    public ResponseEntity<String> stopApp(@PathVariable Long pid) {
        return ResponseEntity.ok(appService.stopApp(pid));
    }
}