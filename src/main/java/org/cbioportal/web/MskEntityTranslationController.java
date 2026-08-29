package org.cbioportal.web;

import java.util.List;

import org.cbioportal.model.MskEntityTranslation;
import org.cbioportal.service.MskEntityTranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class MskEntityTranslationController {

    private final MskEntityTranslationService mskEntityTranslationService;

    @Autowired
    public MskEntityTranslationController(MskEntityTranslationService mskEntityTranslationService) {
        this.mskEntityTranslationService = mskEntityTranslationService;
    }

    @PreAuthorize("hasPermission(#studyId, 'CancerStudyId', 'read')")
    @GetMapping("/studies/{studyId}/msk-entity-translations")
    public ResponseEntity<List<MskEntityTranslation>> getEntitiesForStudy(@PathVariable String studyId) {
        return ResponseEntity.ok(mskEntityTranslationService.getEntityTranslations(studyId));
    }
}
