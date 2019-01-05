package com.greenasjade.j01;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JosekiTreeController {

	private static final Logger logger = LoggerFactory.getLogger(JosekiTreeController.class);
	 
	private BoardPositionStore store;
	private J01Application app;
	
	public JosekiTreeController(BoardPositionStore store, J01Application app) {
		 this.store = store;
		 this.app = app;
	}
	
    @RequestMapping("/root")
    public String root() {
    	BoardPosition retrieved = store.findByPlay("root");
    	logger.info(retrieved.getPlay());
    	return retrieved.toString();
    }

    @RequestMapping("/node")
    public BoardPosition getNodeByCoord(@RequestParam(value="p", defaultValue="root") String p) {
    	BoardPosition node = store.findByPlay(p);
    	logger.info(node.getPlay());
    	return node;
    }
    
    @RequestMapping("/reset")
    public String reset () {
    	app.resetDB(this.store);
    	return "reset done";
    }
   
}
