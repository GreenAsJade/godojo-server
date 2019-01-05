package com.greenasjade.j01;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
public class J01Application {

	private static final Logger log = LoggerFactory.getLogger(J01Application.class);
	
	public static void main(String[] args) {
		SpringApplication.run(J01Application.class, args);
	}

	@Bean
	public CommandLineRunner initialise (BoardPositionStore store) {
		return args -> {
			log.info("Initialising...");
			BoardPosition rootNode = store.findByPlay("root");			
			if (rootNode == null) {
				resetDB(store);
			}
			rootNode = store.findByPlay("root");			
	
			log.info(rootNode.toString());
			
			BoardPosition child = store.findByPlay("root.C1");
			log.info(child.toString());
			
			log.info(rootNode.children.toArray()[0].toString());
		};
	}
	
	void resetDB(BoardPositionStore store) {
		log.info("reloading DB...");
		store.deleteAll();
		BoardPosition rootNode = new BoardPosition("root");
		store.save(rootNode);
		BoardPosition child = rootNode.addMove("C1");
		log.info("created child: "+ rootNode.toString());		
		store.save(rootNode);
		store.save(child);

		rootNode = store.findByPlay("root");
		log.info(rootNode.toString());
		log.info("(done)");
	}	
}
