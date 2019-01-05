package com.greenasjade.j01;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JosekiTreeController {

	private static final Logger logger = LoggerFactory.getLogger(JosekiTreeController.class);
	 
	private BoardPositionStore bp_store;
	private JosekiStore j_store;
	private J01Application app;
	
	public JosekiTreeController(BoardPositionStore bp_store, JosekiStore j_store, J01Application app) {
		 this.bp_store = bp_store;
		 this.j_store = j_store;
		 this.app = app;
	}
	
    @RequestMapping("/root")
    public BoardPosition root() {
    	BoardPosition retrieved = bp_store.findByPlay("root");
    	logger.info(retrieved.getPlay());
    	return retrieved;
    }

    @RequestMapping("/node")
    public BoardPosition getNodeByCoord(@RequestParam(value="p", defaultValue="root") String p) {
    	BoardPosition node = bp_store.findByPlay(p);
    	logger.info(node.getPlay());
    	return node;
    }
    
    @RequestMapping("/reset")
    public String reset () {
    	app.resetDB(this.bp_store, this.j_store);
    	return "reset done";
    }
}
