package org.cbioportal.web;


import static org.mockito.Mockito.mock;
import org.cbioportal.model.MskEntityTranslation;
import org.cbioportal.service.MskEntityTranslationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MskEntityTranslationControllerTest {

    private static final String TEST_STUDY_ID = "study_tcga_pub";
    private MskEntityTranslationService mskEntityTranslationService;

    @InjectMocks
    private MskEntityTranslationController mskEntityTranslationController;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        mskEntityTranslationService = mock(MskEntityTranslationService.class);
        // Single @BeforeEach; standalone MockMvc setup with Spring Security test support
        // so @PreAuthorize is actually enforced at runtime (not just reflectively inspected).
        mockMvc = MockMvcBuilders.standaloneSetup(mskEntityTranslationController)
            .apply(SecurityMockMvcConfigurers.springSecurity())
            .build();
    }

    @Test
    public void getEntitiesForStudy_returnsTranslationsFromService() {
        List<MskEntityTranslation> expected = Collections.singletonList(new MskEntityTranslation());
        when(mskEntityTranslationService.getEntityTranslations(TEST_STUDY_ID)).thenReturn(expected);

        ResponseEntity<List<MskEntityTranslation>> response =
            mskEntityTranslationController.getEntitiesForStudy(TEST_STUDY_ID);

        // Validates ResponseEntity.ok(...) usage (200 + body) per review comment 3886194180
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(expected, response.getBody());
    }

    @Test
    public void getEntitiesForStudy_shouldHavePreAuthorizeStudyReadCheck() throws NoSuchMethodException {
        // Reflection check retained: guarantees the annotation is present on the method
        // (fast, no Spring context needed). Runtime enforcement is covered separately
        // below by the MockMvc + Spring Security test.
        Method method = MskEntityTranslationController.class.getMethod("getEntitiesForStudy", String.class);
        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);

        assertNotNull(preAuthorize, "Expected @PreAuthorize annotation on getEntitiesForStudy");
        assertEquals("hasPermission(#studyId, 'CancerStudyId', 'read')", preAuthorize.value());
    }

    @Test
    @WithAnonymousUser
    public void getEntitiesForStudy_unauthorizedUser_shouldReturn403() throws Exception {
        // Integration-style check via MockMvc + Spring Security: verifies the
        // @PreAuthorize check is actually enforced at runtime for an anonymous caller.
        mockMvc.perform(get("/api/studies/{studyId}/msk-entity-translations", TEST_STUDY_ID)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    public void getEntitiesForStudy_authorizedUser_shouldReturn200() throws Exception {
        when(mskEntityTranslationService.getEntityTranslations(anyString()))
            .thenReturn(Collections.singletonList(new MskEntityTranslation()));

        mockMvc.perform(get("/api/studies/{studyId}/msk-entity-translations", TEST_STUDY_ID)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    public void getEntitiesForStudy_edgeCase_nullStudyIdIsPassedThroughToService() {
        // Kept as a direct unit test of service delegation behavior only.
        // A null path variable cannot occur via a real MVC request (Spring routing
        // would not match the mapping), so this does NOT exercise the controller's
        // MVC/security contract - see the MockMvc tests above for that coverage.
        when(mskEntityTranslationService.getEntityTranslations(null)).thenReturn(null);

        ResponseEntity<List<MskEntityTranslation>> response =
            mskEntityTranslationController.getEntitiesForStudy(null);

        assertNull(response.getBody());
    }
}
