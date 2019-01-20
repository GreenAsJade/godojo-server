package com.greenasjade.godojo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
//import org.springframework.beans.factory.annotation.Value;

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
			if (true) { //rootNode == null) {
				resetDB(bp_store, j_store, m_store);
			}
			rootNode = bp_store.findByPlay("root");

			log.info(rootNode.toString());
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
		rootNode.addComment("test comment");
		rootNode.setTitle("Empty Board");
		rootNode.setDescription("Infinite possibilities await!");

		bp_store.save(rootNode);

		log.info("After save of root: " + rootNode.toString() );

		Long root_id = rootNode.id;

		BoardPosition child;

		child = rootNode.addMove("Q16");
		child = rootNode.addMove("R16");
		child = rootNode.addMove("R17");
		child.setTitle("San San");
		
		child = rootNode.addMove("Q15", MoveCategory.GOOD);
		child = rootNode.addMove("R15", MoveCategory.GOOD);
		
		child = rootNode.addMove("K10", MoveCategory.GOOD);
		child.setTitle("Tengen");
		child.setDescription("Dwyrin's favourite!");
		
		child = rootNode.addMove("S18", MoveCategory.MISTAKE);

		bp_store.save(rootNode);

		log.info("After save of root with children: " + rootNode.toString() );

		Move the_move;

		/* Figuring out how/when/what Neo4j loads */
		log.info("Loading and looking at child moves...");

		the_move = rootNode.children.toArray(new Move[0])[0];
		log.info("After creation, move on the root node: " + the_move.toString() );

		rootNode = bp_store.findByPlay("root");
		log.info("reloaded root: " + rootNode.toString());

		the_move = rootNode.children.iterator().next();
		log.info("After reload, move from root: " + the_move.toString() ); // The move doesn't have it's target loaded

		the_move = child.parent;
		log.info("Child POV, move: " + the_move.toString() );

		the_move = m_store.findByPlacement("Q16");
		log.info("Move direct load: " + the_move.toString());

		/* Test reload of a position */

		rootNode = bp_store.findById(root_id).orElse(null);
		log.info("After findById: " + rootNode.toString() );

		log.info("Adding a joseki...");
		Joseki j1 = new Joseki("Joseki1");

		the_move = m_store.findByPlacement("Q16");
		log.info("First move direct load: " + the_move.toString());

		j1.addMove(the_move);
		j_store.save(j1);

		log.info(j1.toString());

		the_move = m_store.findByPlacement("Q16");
		log.info("First move direct load: " + the_move.toString());

		the_move = m_store.findByPlacement("R16");
		log.info("Second move direct load: " + the_move.toString());

		log.info("Adding second level moves to a node...");

		child.addMove("Q16");
		child.addMove("R16");
		child.addMove("R17");
		bp_store.save(child);

		// Test adding a comment later
		rootNode.addComment("second comment");
		bp_store.save(rootNode);

		log.info("...DB reset done");
	}
}
