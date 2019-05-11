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

    private BoardPositions bp_access;
    private JosekiSources js_access;
    private BoardPositionsNative bpn;
    private Users user_access;

    @Bean
    public CommandLineRunner initialise (
            BoardPositionsNative store_by_bp,
            JosekiSources js_access,
            Users user_access
    ) {
        return args -> {
            log.info("Initialising...");

            this.bpn = store_by_bp;
            this.bp_access = new BoardPositions(store_by_bp);
            this.js_access = js_access;
            this.user_access = user_access;

            BoardPosition rootNode = bp_access.findActiveByPlay(".root");

            if (true) { //rootNode == null) {
                resetDB();
            }
            rootNode = bp_access.findActiveByPlay(".root");

            log.info(rootNode.getInfo());
        };
    }

    void resetDB() {
        log.info("resetting DB...");

        bp_access.deleteEverythingInDB();

        Long GajId = 168L;  // Initial moves came from GreenAsJade!

        // Give GaJ all da powaz...

        User gaj = new User(GajId);
        gaj.setCanEdit(true);
        gaj.setAdministrator(true);
        gaj.setCanComment(true);

        user_access.save(gaj);

        User check = user_access.findByUserId(168L);

        log.info(check.toString());

        // Set up some basic content...

        js_access.save(new JosekiSource("Dwyrin", "http://dwyrin.com", GajId));
        js_access.save(new JosekiSource("Traditional", "", GajId));


        BoardPosition rootNode = new BoardPosition("", "root", GajId);
        rootNode.addComment("test comment", GajId);
        rootNode.setDescription("## Empty Board\n\nInfinite possibilities await!", GajId);

        bp_access.save(rootNode);

        log.info("After save of root: " + rootNode.getInfo() );

        Long root_id = rootNode.id;

        BoardPosition child;
        
        child = rootNode.addMove("Q16", GajId);
        child = rootNode.addMove("R16", GajId);
        child = rootNode.addMove("R17", GajId);
        child.setDescription("## San San", GajId);

        child = rootNode.addMove("Q15", PlayCategory.GOOD, GajId);
        child = rootNode.addMove("R15", PlayCategory.GOOD, GajId);

        child = rootNode.addMove("K10", PlayCategory.GOOD, GajId);
        child.setDescription("## Tengen\nDwyrin's favourite!", GajId);

        child = rootNode.addMove("S18", PlayCategory.MISTAKE, GajId);

        bp_access.save(rootNode);

        log.info("After save of root with children: " + rootNode.getInfo() );

        /* Figuring out how/when/what Neo4j loads */
        log.info("Loading and looking at child moves...");

        rootNode = bp_access.findActiveByPlay(".root");
        log.info("reloaded root: " + rootNode.getInfo());

        /* Test reload of a position */

        rootNode = bp_access.findById(root_id);
        log.info("After findById: " + rootNode.getInfo() );

        log.info("Adding second level moves to a node...");

        child.addMove("Q16", GajId);
        child.addMove("R16", GajId);
        child.addMove("R17",GajId);
        bp_access.save(child);

        // Test adding a comment later
        rootNode.addComment("second comment", GajId);
        bp_access.save(rootNode);

        // Test changing a category
        child.setCategory(PlayCategory.QUESTION, GajId);
        bp_access.save(child);

        log.info("...DB reset done");
    }
}
