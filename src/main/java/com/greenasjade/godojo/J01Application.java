package com.greenasjade.godojo;

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
	public CommandLineRunner initialise (
			BoardPositionStore bp_store, 
			JosekiStore j_store,
			MoveStore m_store) {
		return args -> {
			log.info("Initialising...");
			BoardPosition rootNode = bp_store.findByPlay("root");			
			if (true) {//(rootNode == null) {
				resetDB(bp_store, j_store, m_store);
			}
			rootNode = bp_store.findByPlay("root");			
	
			log.info(rootNode.toString());
			
			BoardPosition child = bp_store.findByPlay("root.C1");
			log.info(child.toString());
			
			Move the_move = m_store.findByPlacement("C1");
			log.info("Move direct load: " + the_move.toString());
			
			Joseki j = j_store.findByName("Joseki1");
			log.info(j.toString());

		};
	}
	
	void resetDB(
			BoardPositionStore bp_store, 
			JosekiStore j_store,
			MoveStore m_store) {
		log.info("reseting DB...");
		bp_store.deleteAll();
		j_store.deleteAll();
		m_store.deleteAll();
		
		BoardPosition rootNode = new BoardPosition("root");
		bp_store.save(rootNode);
		
		BoardPosition child = rootNode.addMove("C1");
		
		log.info("created child: "+ child.toString());		
		bp_store.save(rootNode);

 		Move the_move = rootNode.children.toArray(new Move[0])[0];
		log.info("After creation, move: " + the_move.toString() );
		
		rootNode = bp_store.findByPlay("root");
		log.info("reloaded root: " + rootNode.toString());
		
		child = bp_store.findByPlay("root.C1");
		log.info("reloaded child: " + child.toString());
		
		the_move = rootNode.children.toArray(new Move[0])[0];
		log.info("After reload, move: " + the_move.toString() );
		
		the_move = child.parent;
		log.info("Child POV, move: " + the_move.toString() );	
		
		the_move = m_store.findByPlacement("C1");
		log.info("Move direct load: " + the_move.toString());
		
		Joseki j1 = new Joseki("Joseki1");
		
		the_move = m_store.findByPlacement("C1");
		log.info("Move direct load: " + the_move.toString());
	
		j1.addMove(the_move);
		j_store.save(j1);
		
		log.info(j1.toString());
		
		the_move = m_store.findByPlacement("C1");
		log.info("Move direct load: " + the_move.toString());
			
		log.info("...done");
	}	
}
