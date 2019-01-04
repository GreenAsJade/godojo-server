package com.greenasjade.j01;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ReactLoaderController {

	@RequestMapping(value = "/")
	public String index() {
		return "index";
	}

}