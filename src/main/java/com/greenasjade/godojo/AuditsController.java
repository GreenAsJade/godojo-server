package com.greenasjade.godojo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;

import java.util.ArrayList;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuditsController {

	@Value("${godojo.http.ogs-key}")
	private String ogs_key;

    private static final Logger log = LoggerFactory.getLogger(AuditsController.class);

    private AuditStore audit_store;

    public AuditsController(
            AuditStore audit_store) {
        this.audit_store = audit_store;
    }

    @CrossOrigin()
    @ResponseBody()
    @GetMapping("/godojo/audits" )
    // Return all the information needed to display audit log for a single position
    public ArrayList<Audit> audits(
            @RequestParam(value = "id", required = false, defaultValue = "root") String node_id) {

        if (node_id.equals("root")) {
            node_id = audit_store.getRootId();
        }
        log.info("Audit request for: " + node_id);

        return audit_store.findByNodeId(Long.parseLong(node_id));
    }

    @CrossOrigin()
    @ResponseBody()
    @GetMapping("/godojo/changes" )
    // Return recent change information
    public Page<AuditDTO> changes(Pageable pageable) {

        log.info("Change log request");

        return audit_store.getAudits(pageable).map(audit -> new AuditDTO(audit));
    }
    @CrossOrigin()
    @ResponseBody()
    @PostMapping("/godojo/revert")
    RevertResultDTO revert(
            @RequestHeader("X-User-Info") String user_jwt,
            @RequestBody RevertRequestDTO request) {
        log.info("revert request " + request);

        /* TBD make sure user is authorised before proceeding.
         */


        return new RevertResultDTO("done");
    }

}