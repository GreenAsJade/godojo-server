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
    public CommandLineRunner initialise (BoardPositionStore bp_store) {
        return args -> {
            log.info("Initialising...");

            BoardPosition rootNode = bp_store.findByPlay(".root");
            if (true) { //rootNode == null) {
                resetDB(bp_store);
            }
            rootNode = bp_store.findByPlay(".root");

            log.info(rootNode.toString());
        };
    }

    void resetDB(
            BoardPositionStore bp_store
    ) {
        log.info("reseting DB...");

        bp_store.deleteAll();

        BoardPosition rootNode = new BoardPosition("", "root");
        rootNode.addComment("test comment");
        rootNode.setDescription("## Empty Board\n\nInfinite possibilities await!");

        bp_store.save(rootNode);

        log.info("After save of root: " + rootNode.toString() );

        Long root_id = rootNode.id;

        BoardPosition child;

        child = rootNode.addMove("Q16");
        child = rootNode.addMove("R16");
        child = rootNode.addMove("R17");
        child.setDescription("## San San");

        child = rootNode.addMove("Q15", PlayCategory.GOOD);
        child = rootNode.addMove("R15", PlayCategory.GOOD);

        child = rootNode.addMove("K10", PlayCategory.GOOD);
        child.setDescription("## Tengen\nDwyrin's favourite!");

        child = rootNode.addMove("S18", PlayCategory.MISTAKE);

        bp_store.save(rootNode);

        log.info("After save of root with children: " + rootNode.toString() );

        /* Figuring out how/when/what Neo4j loads */
        log.info("Loading and looking at child moves...");

        rootNode = bp_store.findByPlay(".root");
        log.info("reloaded root: " + rootNode.toString());

        /* Test reload of a position */

        rootNode = bp_store.findById(root_id).orElse(null);
        log.info("After findById: " + rootNode.toString() );

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
