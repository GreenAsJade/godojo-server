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
	public CommandLineRunner initialise (BoardPositionStore bp_store, JosekiStore j_store) {
		return args -> {
			log.info("Initialising...");
			BoardPosition rootNode = bp_store.findByPlay("root");			
			if (1==1) {//(rootNode == null) {
				resetDB(bp_store, j_store);
			}
			rootNode = bp_store.findByPlay("root");			
	
			log.info(rootNode.toString());
			
			BoardPosition child = bp_store.findByPlay("root.C1");
			log.info(child.toString());
			
			log.info("Resulting link: " + rootNode.children.toArray()[0].toString());
		};
	}
	
	void resetDB(BoardPositionStore bp_store, JosekiStore j_store) {
		log.info("reloading DB...");
		bp_store.deleteAll();
		BoardPosition rootNode = new BoardPosition("root");
		bp_store.save(rootNode);
		BoardPosition child = rootNode.addMove("C1");
		log.info("created child: "+ rootNode.toString());		
		bp_store.save(rootNode);
		bp_store.save(child);

		rootNode = bp_store.findByPlay("root");
		log.info(rootNode.toString());
		
		Joseki j1 = new Joseki("Joseki1");
		log.info(j1.toString());
		j_store.save(j1);
		
		Move the_move = rootNode.children.toArray(new Move[0])[0];
		log.info("The move: " + the_move.toString());
		
		j1.addMove(the_move);
		j_store.save(j1);
		
		log.info(j1.toString());		
		log.info("...done");
	}	
}
