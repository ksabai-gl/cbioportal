package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.cbioportal.model.MskEntityTranslation;
import org.cbioportal.service.MskEntityTranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping("/api")
@Api(tags = "Msk Entity Translation", description = " ")
public class MskEntityTranslationController {

    @Autowired
    private MskEntityTranslationService mskEntityTranslationService;

    public MskEntityTranslationController() {
    }

    public MskEntityTranslationController(MskEntityTranslationService mskEntityTranslationService) {
        this.mskEntityTranslationService = mskEntityTranslationService;
    }

    @PreAuthorize("hasPermission(#studyId, 'CancerStudyId', 'read')")
    @GetMapping(value = "/studies/{studyId}/msk-entity-translations", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get MSK entity translations for a study", nickname = "getEntitiesForStudy")
    public ResponseEntity<List<MskEntityTranslation>> getEntitiesForStudy(
            @ApiParam(required = true, value = "Study ID e.g. acc_tcga")
            @PathVariable @NotNull String studyId) {

        return ResponseEntity.ok(mskEntityTranslationService.getEntityTranslations(studyId));
    }
}
