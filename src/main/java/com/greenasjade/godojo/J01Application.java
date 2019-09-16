package com.greenasjade.godojo;

import io.sentry.Sentry;
import io.sentry.event.BreadcrumbBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@SpringBootApplication
@EnableAsync
public class J01Application {

    private static final Logger log = LoggerFactory.getLogger(J01Application.class);

    public static void main(String[] args) {
        SpringApplication.run(J01Application.class, args);
    }

    public static void debug(String message, Logger logger) {
        // breadcrumbs to Sentry for debug on issues
        Sentry.getContext().recordBreadcrumb(
                new BreadcrumbBuilder().setMessage(message).build()
        );

        logger.debug(message);
    }

    private BoardPositionsNative native_bp_access;
    private BoardPositions bp_access;
    private JosekiSources js_access;
    private Tags tags_access;
    private Users user_access;
    private AppInfos app_info_access;
    private Audits audit_access;

    private Integer target_schema = 9;

    @Value("${sentry.environment}")
    private String environment;

    @Bean
    public CommandLineRunner initialise (
            BoardPositionsNative native_bp_access,
            JosekiSources js_access,
            Tags tags_access,
            Users user_access,
            AppInfos app_info_access,
            Audits audit_access
    ) {
        return args -> {
            J01Application.debug("Initialising...", log);

            if (environment.equals("set-me-in-the-environment")) {
                throw new RuntimeException("SENTRY_ENVIRONMENT needs to be set");
            }
            J01Application.debug("Server environment: " + environment, log);

            this.native_bp_access = native_bp_access;
            this.bp_access = new BoardPositions(native_bp_access);
            this.js_access = js_access;
            this.tags_access = tags_access;
            this.user_access = user_access;
            this.app_info_access = app_info_access;
            this.audit_access = audit_access;

            // note: there should only be one (or zero) app_info in app_infos!
            AppInfo app_info = this.app_info_access.getAppInfo();

            if (app_info == null) {
                app_info = new AppInfo();
                app_info.setSchema_id(0);
                J01Application.debug("*** Initialising Schema ID", log);
            }

            Integer db_schema = app_info.getSchema_id();

            if (db_schema < target_schema) {
                log.info("Migrating from " + db_schema.toString() + " to " + target_schema.toString());
                migrateToSchema(target_schema, db_schema);
            }
            else {
                log.info("Currently on schema version " + target_schema.toString());
            }

            BoardPosition rootNode = bp_access.findActiveByPlay(".root");

            if (rootNode == null) {
                resetDB();
            }
            rootNode = bp_access.findActiveByPlay(".root");

            J01Application.debug("Current root: " + rootNode.getInfo(), log);
        };
    }

    void migrateToSchema(Integer schema_id, Integer previous_schema){
        AppInfo app_info;

        switch (schema_id) {
            case 2:
                // This is the introduction of BoardPosition.variation_label
                // with the deprecation of BoardPosition.seq,
                // plus introducing tags

                // lets just reset the DB for this  <<< *** BEWARE OF THIS MIGRATION!
                resetDB();

                app_info = new AppInfo();
                app_info.setSchema_id(schema_id);
                app_info_access.save(app_info);
                return;

            case 3:

                /* theoretically we'd do this, but I don't want any risk of inadvertent
                   database reset... */
                /*if (previous_schema < 2) {
                    migrateToSchema(2, previous_schema);
                }*/

                this.changeToNumericVariationLabels();
                this.addOutcomeTags();

                break;

            case 4:
                if (previous_schema != 3) {
                    log.error("Expecting schema level 3.  Can't update to schema level 4!");
                    throw new RuntimeException("Unexpected schema level");
                }

                Tag currency_tag = new Tag("Current");
                currency_tag.setGroup(0);
                currency_tag.setSeq(3);
                tags_access.save(currency_tag);

                break;

            case 5:
                if (previous_schema != 4) {
                    log.error("Expecting schema level 4.  Can't update to schema level 5!");
                    throw new RuntimeException("Unexpected schema level");
                }

                Tag corner_tag = new Tag("Black gets the corner");
                corner_tag.setGroup(1);
                corner_tag.setSeq(0);
                tags_access.save(corner_tag);

                corner_tag = new Tag("White gets the corner");
                corner_tag.setGroup(2);
                corner_tag.setSeq(0);
                tags_access.save(corner_tag);

                Tag sente_tag = new Tag("Black gets sente");
                sente_tag.setGroup(3);
                sente_tag.setSeq(0);
                tags_access.save(sente_tag);

                sente_tag = new Tag("White gets sente");
                sente_tag.setGroup(3);
                sente_tag.setSeq(1);
                tags_access.save(sente_tag);
                break;

            case 6:
                log.info("Migrating to schema 6");
                this.fixVariationLabelZeros();
                break;

            case 7:
                if (previous_schema < 6) {
                    this.migrateToSchema(6, previous_schema);
                }
                // nothing to do now for schema 7
                log.info("Migrating to schema 7");
                J01Application.debug("No migration to run for schema 7", log);
                break;

            case 8:
                if (previous_schema < 7) {
                    this.migrateToSchema(7, previous_schema);
                }

                log.info("Migrating to schema 8");
                if (environment.equals("production")) {
                    this.migrateUsersToProductionIds();
                }
                else {
                    log.warn("Not on Production so not migrating user IDs");
                }
                break;

            case 9:
                if (previous_schema != 8) {
                    this.migrateToSchema(8, previous_schema);
                }

                log.info("Migrating to schema 9");
                if (environment.equals("production")) {
                    this.migrateAuditsToProductionUserIds();
                }
                else {
                    log.warn("Not on Production so not migrating user IDs");
                }
                break;

            case 999: // we'll do this later

                this.loadStressTest();
                break;

            default:
                // should maybe throw here I guess
                log.error("Unrecognised schema ID! " + schema_id);
        }

        app_info = this.app_info_access.getAppInfo();
        app_info.setSchema_id(schema_id);
        app_info_access.save(app_info);
        return;
    }

    void resetDB() {
        J01Application.debug("resetting DB...", log);

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

            J01Application.debug(check.toString(), log);
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

        J01Application.debug("After save of root: " + rootNode.getInfo(), log);

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

        J01Application.debug("After save of root with children: " + rootNode.getInfo(), log);

        /* Figuring out how/when/what Neo4j loads */
        J01Application.debug("Loading and looking at child moves...", log);

        rootNode = bp_access.findActiveByPlay(".root");
        J01Application.debug("reloaded root: " + rootNode.getInfo(), log);

        /* Test reload of a position */

        rootNode = bp_access.findById(root_id);
        J01Application.debug("After findById: " + rootNode.getInfo(), log);

        J01Application.debug("Adding second level moves to a node...", log);

        // Test adding a comment later
        rootNode.addComment("... initial setup in place!", GajId);
        bp_access.save(rootNode);

        // Test changing a category
        child.setCategory(PlayCategory.QUESTION, GajId);
        bp_access.save(child);

        J01Application.debug("...DB reset done", log);
    }


    void changeToNumericVariationLabels() {
        // Here we transition from 'A' 'B' 'C' to '1' '2' '3' for variation IDs

        this.native_bp_access.findAll().forEach(p->{
            Character v = p.getVariationLabel();
            Character n;
            if (v != null && "ABCDEFG".indexOf(v) != -1) {
                n = (char) (v - 'A' + '1');
            } else {
                n = v;
            }
            J01Application.debug("converting " + v + " to " + n, log);
            p.setVariationLabel(n);
            bp_access.save(p);
        });
        return;
    }

    void addOutcomeTags() {
        // Update existing tag's group and seq

        List<Tag> tags = tags_access.listTags();

        for (int i = 0; i < tags.size(); i++) {
            Tag tag = tags.get(i);
            tag.setGroup(0);
            tag.setSeq(i);
            tags_access.save(tag);
        }

        // And add some outcome tags

        Tag result_tag;

        result_tag = new Tag("Black gets the corner and top", 1, 1);
        tags_access.save(result_tag);
        result_tag = new Tag("Black gets the corner and center", 1, 2);
        tags_access.save(result_tag);
        result_tag = new Tag("Black gets the corner and right", 1, 3);
        tags_access.save(result_tag);
        result_tag = new Tag("Black gets the corner, top and center", 1, 4);
        tags_access.save(result_tag);
        result_tag = new Tag("Black gets the corner, top and right", 1, 5);
        tags_access.save(result_tag);
        result_tag = new Tag("Black gets the corner, center and right", 1, 6);
        tags_access.save(result_tag);
        result_tag = new Tag("Black gets the top", 1, 7);
        tags_access.save(result_tag);
        result_tag = new Tag("Black gets the centre", 1, 8);
        tags_access.save(result_tag);
        result_tag = new Tag("Black gets the right", 1, 9);
        tags_access.save(result_tag);
        result_tag = new Tag("Black gets the top and centre", 1, 10);
        tags_access.save(result_tag);
        result_tag = new Tag("Black gets the top and right", 1, 11);
        tags_access.save(result_tag);
        result_tag = new Tag("Black gets the centre and right", 1, 12);
        tags_access.save(result_tag);
        result_tag = new Tag("White gets the corner and top", 2, 1);
        tags_access.save(result_tag);
        result_tag = new Tag("White gets the corner and center", 2, 2);
        tags_access.save(result_tag);
        result_tag = new Tag("White gets the corner and right", 2, 3);
        tags_access.save(result_tag);
        result_tag = new Tag("White gets the corner, top and center", 2, 4);
        tags_access.save(result_tag);
        result_tag = new Tag("White gets the corner, top and right", 2, 5);
        tags_access.save(result_tag);
        result_tag = new Tag("White gets the corner, center and right", 2, 6);
        tags_access.save(result_tag);
        result_tag = new Tag("White gets the top", 2, 7);
        tags_access.save(result_tag);
        result_tag = new Tag("White gets the centre", 2, 8);
        tags_access.save(result_tag);
        result_tag = new Tag("White gets the right", 2, 9);
        tags_access.save(result_tag);
        result_tag = new Tag("White gets the top and centre", 2, 10);
        tags_access.save(result_tag);
        result_tag = new Tag("White gets the top and right", 2, 11);
        tags_access.save(result_tag);
        result_tag = new Tag("White gets the centre and right", 2, 12);
        tags_access.save(result_tag);

        return;
    }

    void fixVariationLabelZeros() {
        native_bp_access.streamVariationLabelZeros().forEach( p -> {
            BoardPosition the_position = native_bp_access.findById(p.id).orElse(null); // load children
            J01Application.debug("Fixing variation label at node " + the_position.getInfo(), log);
            the_position.setVariationLabel((char)(the_position.parent.nextVariationLabel() -1));
            native_bp_access.save(the_position);
        });
    }

    void migrateUsersToProductionIds() {
        Map<Long, Long> userIdMap = new HashMap<Long, Long>() {
            {
                put(1L, 1L);  // Anoek
                put(129L, 64817L);   // Mark5000
                put(168L, 412892L);  // Eugene
                put(645L, 499745L);  // DevGaj -> GaJ
                put(816L, 360861L);  // AdamR
                put(913L, 445315L);  // BHydden
                put(920L, 427361L);  // shinuito
                put(923L, 639990L);  // Icedrinker
            }
        };

        user_access.streamAllUsers().forEach( u -> {
            Long newId = userIdMap.get(u.getUserId());

            if (newId == null) {
                throw new RuntimeException("Unmappable user ID:" + u.getUserId().toString());
            }

            J01Application.debug("User ID update for user: " + u.getUserId(), log);
            J01Application.debug(" -> " + newId, log);
            u.setUserId(newId);
            user_access.save(u);
        });

        J01Application.debug("Doing contributor ID updates...", log);
        native_bp_access.streamAllPositions().forEach( p -> {
            Long newId = userIdMap.get(p.getContributorId());

            if (newId == null) {
                throw new RuntimeException("Unmappable contributor ID:" + p.getContributorId().toString());
            }

            BoardPosition full = bp_access.findById(p.id); // load relationships

            full.setContributorId(newId);

            if (full.commentary != null && full.commentary.size() > 0) {
                J01Application.debug("Found comments: " + full.commentary.toString(), log);
                full.commentary.stream().forEach(c -> {
                    Long newCid = userIdMap.get(c.getUserId());

                    if (newCid == null) {
                        throw new RuntimeException("Unmappable commenter ID:" + c.getUserId().toString());
                    }

                    c.setUserId(newCid);
                });
            }
            native_bp_access.save(full);
        });
    }

    void migrateAuditsToProductionUserIds() {
        Map<Long, Long> userIdMap = new HashMap<Long, Long>() {
            {
                put(1L, 1L);  // Anoek
                put(129L, 64817L);   // Mark5000
                put(168L, 412892L);  // Eugene
                put(645L, 499745L);  // DevGaj -> GaJ
                put(816L, 360861L);  // AdamR
                put(913L, 445315L);  // BHydden
                put(920L, 427361L);  // shinuito
                put(923L, 639990L);  // Icedrinker
            }
        };

        log.info("Doing Audit ID updates...");
        audit_access.streamAllAudits().forEach( a -> {
            Long newId = userIdMap.get(a.getUserId());

            if (newId == null) {
                throw new RuntimeException("Unmappable user ID:" + a.getUserId().toString());
            }

            a.setUserId(newId);
            audit_access.save(a);
        });
    }

    void loadStressTest() {

        BoardPosition start = this.bp_access.findActiveByPlay(".root");
        this.extendSequence(start,'A', 1, 50000);
        J01Application.debug("Writing lots of nodes to neo - can take a few minutes on my machine...", log);
        bp_access.save(start);

        start = this.bp_access.findActiveByPlay(".root"); // dump old node, allow it to be freed
        this.extendSequence(start,'A', 2, 100000);
        J01Application.debug("Writing lots of nodes to neo - can take a few minutes on my machine...", log);
        bp_access.save(start);

        start = this.bp_access.findActiveByPlay(".root");
        this.extendSequence(start,'A', 3, 150000);
        J01Application.debug("Writing lots of nodes to neo - can take a few minutes on my machine...", log);
        bp_access.save(start);

        start = this.bp_access.findActiveByPlay(".root");
        this.extendSequence(start,'A', 4, 200000);
        J01Application.debug("Writing lots of nodes to neo - can take a few minutes on my machine...", log);
        bp_access.save(start);
    }

    private static Integer loadNodeCount = 0;

    void extendSequence(BoardPosition parent, char x, Integer y, Integer target_count){
        String new_move = x + y.toString();
        BoardPosition new_node = parent.addMove(new_move, PlayCategory.IDEAL, 168L);
        new_node.setDescription("Load-test position", 168L);
        new_node.setCategory(PlayCategory.TRICK, 168L);
        if (this.loadNodeCount % 100 == 0) {
            J01Application.debug("Node count: " + this.loadNodeCount.toString(), log);
        }
        if (y == 1) {
            J01Application.debug(x + "," + y, log);
        }

        this.loadNodeCount += 1;

        // safety valve
        if (x=='A' && y == 19) {
            return;
        }
        if (this.loadNodeCount < target_count) {
            if (x < 'H') {
                this.extendSequence(new_node, (char) (x + 1), y, target_count);
            }
        }
        if (this.loadNodeCount < target_count) {
            if (y < 19) {
                this.extendSequence(new_node, x, y + 1, target_count);
            }
            else {
                Tag random_tag = tags_access.listTags().get(0);
                new_node.setTag(random_tag);
            }
        }
    }
}
