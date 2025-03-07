package com.launchpad.framework.controllers;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class IndexController implements ErrorController {

	@RequestMapping(value = "${server.error.path:${error.path:/error}}")
	public String error() {
		return "forward:/index.html";
	}

	@GetMapping("/{path:[^\\.]*}")  // Serve Angular pages
	public String forward() {
		return "forward:/index.html";
	}

}
