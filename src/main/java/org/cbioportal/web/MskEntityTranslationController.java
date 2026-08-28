package org.cbioportal.web;

import org.cbioportal.service.MskEntityTranslationService;
import org.cbioportal.web.EntityTranslation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * NOTE (SCRUM-87): This file was reconstructed from cBioPortal's standard
 * study-access-control convention because live file contents could not be
 * fetched via GitHub OAuth / Bitbucket PAT prefetch in this run. Verify
 * method signatures and any additional endpoints against the actual source
 * before merging; add @PreAuthorize to ALL study-data-returning methods,
 * not only the one shown here.
 */
@RestController
@RequestMapping("/api/msk-entity-translation")
public class MskEntityTranslationController {

    @Autowired
    private MskEntityTranslationService mskEntityTranslationService;

    @PreAuthorize("hasPermission(#studyId, 'CancerStudyId', 'read')")
    @GetMapping("/studies/{studyId}/entities")
    public ResponseEntity<List<EntityTranslation>> getEntitiesForStudy(
            @PathVariable String studyId) {
        return new ResponseEntity<>(
            mskEntityTranslationService.getEntityTranslations(studyId), HttpStatus.OK);
    }
}
