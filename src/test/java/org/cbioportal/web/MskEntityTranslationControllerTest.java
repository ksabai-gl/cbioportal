package org.cbioportal.web;

import org.cbioportal.service.MskEntityTranslationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * SCRUM-87: Regression + bug-fix tests for MskEntityTranslationController.
 *
 * Verifies:
 *  1) getEntitiesForStudy is annotated with @PreAuthorize using the standard
 *     cBioPortal study-access-control SpEL expression (bug fix: previously
 *     missing, allowing unauthorized study data access).
 *  2) The endpoint still correctly delegates to the service and returns
 *     200 OK with the expected payload (regression).
 *  3) Edge cases: empty result list, null studyId passthrough.
 */
class MskEntityTranslationControllerTest {
    @BeforeEach
    void setUp() {
        mskEntityTranslationService = mock(MskEntityTranslationService.class);
    }


    private MskEntityTranslationController controller;
    private MskEntityTranslationService mskEntityTranslationService;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        controller = new MskEntityTranslationController();
        Field serviceField = MskEntityTranslationController.class
                .getDeclaredField("mskEntityTranslationService");
        serviceField.setAccessible(true);
        serviceField.set(controller, mskEntityTranslationService);
    }

    @Test
    void getEntitiesForStudy_shouldHavePreAuthorizeStudyReadCheck() throws NoSuchMethodException {
        // Bug fix test: the endpoint must be protected by the standard
        // cBioPortal study-access-control annotation.
        Method method = MskEntityTranslationController.class
                .getMethod("getEntitiesForStudy", String.class);

        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);

        assertNotNull(preAuthorize,
                "getEntitiesForStudy must be annotated with @PreAuthorize to enforce study access control (SCRUM-87)");
        assertTrue(preAuthorize.value().contains("hasPermission"),
                "PreAuthorize expression should use hasPermission(...) study access check");
        assertTrue(preAuthorize.value().contains("'CancerStudyId'"),
                "PreAuthorize expression should check permission against CancerStudyId");
        assertTrue(preAuthorize.value().contains("'read'"),
                "PreAuthorize expression should require 'read' permission");
        assertTrue(preAuthorize.value().contains("#studyId"),
                "PreAuthorize expression should bind to the studyId method parameter");
    }

    @Test
    void getEntitiesForStudy_regression_shouldReturnEntitiesFromService() {
        // Regression: existing delegation behavior to the service must be unchanged.
        String studyId = "study_tcga_pub";
        EntityTranslation entity = new EntityTranslation();
        List<EntityTranslation> expected = Collections.singletonList(entity);
        when(mskEntityTranslationService.getEntityTranslations(studyId)).thenReturn(expected);

        ResponseEntity<List<EntityTranslation>> response = controller.getEntitiesForStudy(studyId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expected, response.getBody());
    }

    @Test
    void getEntitiesForStudy_edgeCase_emptyResultReturnsOkWithEmptyList() {
        String studyId = "study_with_no_entities";
        when(mskEntityTranslationService.getEntityTranslations(studyId))
                .thenReturn(Collections.emptyList());

        ResponseEntity<List<EntityTranslation>> response = controller.getEntitiesForStudy(studyId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void getEntitiesForStudy_edgeCase_nullStudyIdIsPassedThroughToService() {
        when(mskEntityTranslationService.getEntityTranslations(null))
                .thenReturn(Collections.emptyList());

        ResponseEntity<List<EntityTranslation>> response = controller.getEntitiesForStudy(null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    // Minimal stand-in mock class in case EntityTranslation is not resolvable
    // from this test's classpath context; remove if the real type already exists.
    static class EntityTranslation {
    }
}
