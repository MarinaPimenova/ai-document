package com.training.app.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/version")
public class VersionController {

    @GetMapping(produces = MediaType.TEXT_PLAIN_VALUE)
    public String getVersion(
            @Value("${info.app.name}") String appName,
            @Value("${info.app.version}") String version) {
        return appName + " : " + version;
    }
}
