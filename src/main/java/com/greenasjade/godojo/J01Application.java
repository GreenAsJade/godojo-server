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
            BoardPositionStore store_by_bp,
            JosekiSourceStore store_by_js
    ) {
        return args -> {
            log.info("Initialising...");

            BoardPosition rootNode = store_by_bp.findByPlay(".root");
            if (true) { //rootNode == null) {
                resetDB(store_by_bp, store_by_js);
            }
            rootNode = store_by_bp.findByPlay(".root");

            log.info(rootNode.toString());
        };
    }

    void resetDB(
            BoardPositionStore store_by_bp,
            JosekiSourceStore store_by_js) {
        log.info("resetting DB...");

        store_by_bp.deleteEverythingInDB();

        Long GajId = 168L;  // Initial moves came from GreenAsJade!

        store_by_js.save(new JosekiSource("Dwyrin", "http://dwyrin.com", GajId));

        BoardPosition rootNode = new BoardPosition("", "root", GajId);
        rootNode.addComment("test comment", GajId);
        rootNode.setDescription("## Empty Board\n\nInfinite possibilities await!");

        store_by_bp.save(rootNode);

        log.info("After save of root: " + rootNode.toString() );

        Long root_id = rootNode.id;

        BoardPosition child;
        
        child = rootNode.addMove("Q16", GajId);
        child = rootNode.addMove("R16", GajId);
        child = rootNode.addMove("R17", GajId);
        child.setDescription("## San San");

        child = rootNode.addMove("Q15", PlayCategory.GOOD, GajId);
        child = rootNode.addMove("R15", PlayCategory.GOOD, GajId);

        child = rootNode.addMove("K10", PlayCategory.GOOD, GajId);
        child.setDescription("## Tengen\nDwyrin's favourite!");

        child = rootNode.addMove("S18", PlayCategory.MISTAKE, GajId);

        store_by_bp.save(rootNode);

        log.info("After save of root with children: " + rootNode.toString() );

        /* Figuring out how/when/what Neo4j loads */
        log.info("Loading and looking at child moves...");

        rootNode = store_by_bp.findByPlay(".root");
        log.info("reloaded root: " + rootNode.toString());

        /* Test reload of a position */

        rootNode = store_by_bp.findById(root_id).orElse(null);
        log.info("After findById: " + rootNode.toString() );

        log.info("Adding second level moves to a node...");

        child.addMove("Q16", GajId);
        child.addMove("R16", GajId);
        child.addMove("R17",GajId);
        store_by_bp.save(child);

        // Test adding a comment later
        rootNode.addComment("second comment", GajId);
        store_by_bp.save(rootNode);

        log.info("...DB reset done");
    }
}
