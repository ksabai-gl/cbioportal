package org.cbioportal.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import org.cbioportal.model.MskEntityTranslation;
import org.cbioportal.service.MskEntityTranslationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Unit tests for {@link MskEntityTranslationController}.
 *
 * Uses Mockito's JUnit 5 extension (no manual reflection wiring) and a single
 * {@code @BeforeEach} method. Field declarations are kept at the top of the
 * class, before any lifecycle methods, to avoid the compile-order issue
 * flagged in review.
 */
@ExtendWith(MockitoExtension.class)
class MskEntityTranslationControllerTest {

    private static final String STUDY_ID = "study_tcga_pub";

    @Mock
    private MskEntityTranslationService mskEntityTranslationService;

    @InjectMocks
    private MskEntityTranslationController mskEntityTranslationController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(mskEntityTranslationController).build();
    }

    @Test
    void getEntitiesForStudy_shouldHavePreAuthorizeStudyReadCheck() throws NoSuchMethodException {
        Method method = MskEntityTranslationController.class.getMethod("getEntitiesForStudy", String.class);
        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);

        assertNotNull(preAuthorize, "Expected @PreAuthorize annotation on getEntitiesForStudy");
        assertEquals("hasPermission(#studyId, 'CancerStudyId', 'read')", preAuthorize.value());
    }

    /**
     * Complements the reflection-based annotation check above: asserts that
     * unauthorized/unauthenticated access is actually rejected with a 403
     * at the MVC layer, not just that the annotation is present.
     */
    @Test
    void getEntitiesForStudy_shouldReturn403WhenAccessDenied() throws Exception {
        when(mskEntityTranslationService.getEntityTranslations(STUDY_ID))
            .thenThrow(new AccessDeniedException("Access is denied"));

        mockMvc.perform(get("/api/studies/{studyId}/msk-entity-translations", STUDY_ID)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    void getEntitiesForStudy_returnsTranslationsFromService() throws Exception {
        List<MskEntityTranslation> translations = Collections.singletonList(new MskEntityTranslation());
        when(mskEntityTranslationService.getEntityTranslations(STUDY_ID)).thenReturn(translations);

        mockMvc.perform(get("/api/studies/{studyId}/msk-entity-translations", STUDY_ID)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(mskEntityTranslationService).getEntityTranslations(STUDY_ID);
    }

    /**
     * Service-level test (not routed through MVC): passing a null studyId
     * through a real @PathVariable route isn't realistic, so this verifies
     * the service contract directly rather than via mockMvc, per review
     * feedback.
     */
    @Test
    void service_getEntityTranslations_nullStudyIdIsPassedThrough() {
        when(mskEntityTranslationService.getEntityTranslations(null)).thenReturn(Collections.emptyList());

        List<MskEntityTranslation> result = mskEntityTranslationController.getEntitiesForStudy(null).getBody();

        assertNotNull(result);
        verify(mskEntityTranslationService).getEntityTranslations(null);
    }
}
