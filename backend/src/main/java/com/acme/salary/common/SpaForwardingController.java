package com.acme.salary.common;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * In the packaged app the Angular build is served from {@code classpath:/static}. Client-side routes
 * (e.g. a bookmarked {@code /insights/IN}) are forwarded to {@code index.html} so a browser refresh works.
 */
@Controller
public class SpaForwardingController {

    @GetMapping({"/employees", "/employees/**", "/insights", "/insights/**"})
    public String forwardToIndex() {
        return "forward:/index.html";
    }
}
