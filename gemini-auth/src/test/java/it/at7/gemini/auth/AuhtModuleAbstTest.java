package it.at7.gemini.auth;

import it.at7.gemini.auth.core.AuthModuleRef;
import it.at7.gemini.auth.core.UserRef;
import it.at7.gemini.core.EntityManager;
import it.at7.gemini.core.EntityRecord;
import it.at7.gemini.core.FilterContext;
import it.at7.gemini.core.Services;
import it.at7.gemini.exceptions.GeminiException;
import it.at7.gemini.schema.Entity;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.List;

import static it.at7.gemini.api.MockMVCUtils.mockMvc;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AuhtModuleAbstTest {

    @Test
    public void n1_testAuthModuleInitialization() throws GeminiException {
        EntityManager entityManager = Services.getEntityManager();
        Entity entity = entityManager.getEntity(UserRef.NAME);
        List<EntityRecord> records = entityManager.getRecordsMatching(entity, FilterContext.withGeminiSearchString(UserRef.FIELDS.USERNAME + " == " + AuthModuleRef.USERS.GEMINI));
        Assert.assertEquals(1, records.size());
        EntityRecord geminiUserRec = records.get(0);
        Assert.assertNotNull(geminiUserRec);

        EntityRecord oAuthClient = new EntityRecord(entityManager.getEntity("OAuthClient"));
        oAuthClient.put("clientId", "client-gui");
        entityManager.putIfAbsent(oAuthClient);
    }

    @Test
    public void n2_testUserPwdAuthWorks() throws Exception {
        mockMvc.perform(post("/oauth/token")
                .contentType(APPLICATION_FORM_URLENCODED)
                .content("client_id=client-gui&grant_type=password&username=Admin&password=Admin")
                .accept(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("access_token").exists())
                .andExpect(jsonPath("refresh_token").exists());
    }
}
