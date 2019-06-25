package com.greenasjade.godojo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;

@SpringBootApplication
public class J01Application {

    private static final Logger log = LoggerFactory.getLogger(J01Application.class);

    public static void main(String[] args) {
        SpringApplication.run(J01Application.class, args);
    }

    private BoardPositions bp_access;
    private JosekiSources js_access;
    private Tags tags_access;
    private Users user_access;
    private AppInfos app_info_access;

    private Integer current_schema = 2;

    @Bean
    public CommandLineRunner initialise (
            BoardPositionsNative store_by_bp,
            JosekiSources js_access,
            Tags tags_access,
            Users user_access,
            AppInfos app_info_access
    ) {
        return args -> {
            log.info("Initialising...");

            this.bp_access = new BoardPositions(store_by_bp);
            this.js_access = js_access;
            this.tags_access = tags_access;
            this.user_access = user_access;
            this.app_info_access = app_info_access;

            // note: there should only be one (or zero) app_info in app_infos!
            Iterable<AppInfo> app_info = app_info_access.findAll();

            // First introduction of schema id
            if (!app_info.iterator().hasNext() || app_info.iterator().next().getSchema_id() < current_schema) {
                AppInfo new_info = new AppInfo();
                new_info.setSchema_id(current_schema);
                migrateToSchema(current_schema);
                app_info_access.save(new_info);
            }

            BoardPosition rootNode = bp_access.findActiveByPlay(".root");

            if (rootNode == null) {
                resetDB();
            }
            rootNode = bp_access.findActiveByPlay(".root");

            log.info(rootNode.getInfo());
        };
    }

    void migrateToSchema(Integer schema_id){
        switch (schema_id) {
            case 2:
                // At the moment we are just resetting the DB with each update...

                // This is the introduction of BoardPosition.variation_label
                // with the deprecation of BoardPosition.seq,
                // plus introducing tags
                log.info("Migrating to schema 2...");

                // lets just reset the DB for this
                resetDB();

                return;
            default:
                // should maybe throw here I guess
                log.error("Unrecognised schema ID! " + schema_id);
        }

    }

    void resetDB() {
        log.info("resetting DB...");

        bp_access.deleteEverythingInDB();

        Long GajId = 168L;  // Initial moves came from GreenAsJade!  This is GaJ Beta ID.

        Long AnoekId = 1L;  // He get da powaz too

        // Give'm all da powaz...

        for (Long admin_id: Arrays.asList(GajId, AnoekId)) {
            User admin = new User(admin_id);
            admin.setCanEdit(true);
            admin.setAdministrator(true);
            admin.setCanComment(true);

            user_access.save(admin);

            User check = user_access.findByUserId(admin_id);

            log.info(check.toString());
        }

        // Lets make another couple of contributors, to test filtering
        // One that is me.

        Long DevGajId = 645L;

        User devgaj = new User(DevGajId);
        devgaj.setCanEdit(true);
        user_access.save(devgaj);

        // One contributor that is not me

        Long MikeId = 913L;

        User mike = new User(MikeId);
        mike.setCanEdit(true);
        user_access.save(mike);

        // Set up the basic content...

        JosekiSource dwyrin = new JosekiSource("Dwyrin", "https://www.patreon.com/dwyrin", GajId);
        JosekiSource traditional = new JosekiSource("Traditional", "", GajId);
        JosekiSource mark5000 =  new JosekiSource("Mark5000", "https://online-go.com/player/64817/", GajId);
        JosekiSource senseis = new JosekiSource("Sensei's Library", "https://senseis.xmp.net", GajId);

        js_access.save(dwyrin);
        js_access.save(traditional);
        js_access.save(mark5000);
        js_access.save(senseis);

        Tag joseki_tag = new Tag("Joseki: Position is settled");
        Tag fuseki_tag = new Tag("Fuseki: Done");

        tags_access.save(joseki_tag);
        tags_access.save(fuseki_tag);

        BoardPosition rootNode = new BoardPosition("", "root", GajId);
        rootNode.addComment("Let's do this thing...", GajId);
        rootNode.setDescription("## Empty Board\n\nInfinite possibilities await!", GajId);

        bp_access.save(rootNode);

        log.info("After save of root: " + rootNode.getInfo() );

        Long root_id = rootNode.id;

        BoardPosition child;
        
        child = rootNode.addMove("Q16", GajId);
        child.setVariationLabel('A');

        // First basic joseki

        child = child.addMove("R14", GajId);
        child = child.addMove("O17", GajId);
        child = child.addMove("S16", GajId);
        child = child.addMove("R17", GajId);
        child = child.addMove("R11", GajId);
        child.setTag(joseki_tag);
        child.setDescription("## Joseki", GajId);
        child.source = traditional;

        bp_access.save(child);

        // Some more nodes
        child = rootNode.addMove("R16", GajId);
        child.setVariationLabel('B');
        child = rootNode.addMove("R17", DevGajId);
        child.setVariationLabel('C');
        child.setDescription("## San San", GajId);

        // A joseki by contributed someone else

        child = bp_access.findActiveByPlay(".root.Q16");
        child = child.addMove("R17", DevGajId);
        child = child.addMove("R16", DevGajId);
        child = child.addMove("Q17", DevGajId);
        child = child.addMove("P17", DevGajId);
        child = child.addMove("P18", DevGajId);
        child = child.addMove("O17", DevGajId);
        child = child.addMove("O18", DevGajId);
        child = child.addMove("N17", DevGajId);
        child.setDescription("Black can tenuki", DevGajId);
        child.source = mark5000;
        child = child.addMove("N18", DevGajId);
        child = child.addMove("M17", DevGajId);
        child.source = mark5000;
        child.setTag(joseki_tag);
        child.setDescription("## Joseki\n\n[4-4 Alpha Go Joseki](https://online-go.com/puzzle/8671)", DevGajId);

        bp_access.save(child);

        // Some more other nodes

        child = rootNode.addMove("Q15", PlayCategory.GOOD, MikeId);
        child = rootNode.addMove("R15", PlayCategory.GOOD, MikeId);

        child = rootNode.addMove("K10", PlayCategory.GOOD, GajId);
        child.setDescription("## Tengen\nDwyrin's favourite!\n\nhttp://www.dwyrin.tv/park-jungwhan-takes-on-tengen/", GajId);
        child.source = dwyrin;
        child.addComment("OK, it's not really his favourite :D", GajId);

        child=child.addMove("Q16", PlayCategory.GOOD, GajId);
        child=child.addMove("D16", PlayCategory.GOOD, GajId);
        child=child.addMove("Q4", PlayCategory.GOOD, GajId);
        child=child.addMove("D4", PlayCategory.GOOD, GajId);
        child.setDescription("## Tengen Sanrensei\n\nhttps://senseis.xmp.net/?TengenFuseki", GajId);
        child.source = senseis;
        child.setTag(fuseki_tag);

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

        // Test adding a comment later
        rootNode.addComment("... initial setup in place!", GajId);
        bp_access.save(rootNode);

        // Test changing a category
        child.setCategory(PlayCategory.QUESTION, GajId);
        bp_access.save(child);

        log.info("...DB reset done");
    }
}
