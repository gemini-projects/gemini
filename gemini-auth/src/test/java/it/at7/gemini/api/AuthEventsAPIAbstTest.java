package it.at7.gemini.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.at7.gemini.UnitTestBase;
import it.at7.gemini.auth.core.AuthMetaRef;
import it.at7.gemini.auth.core.AuthModuleRef;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;

import static it.at7.gemini.api.ApiUtility.GEMINI_API_META_TYPE;
import static it.at7.gemini.api.ApiUtility.GEMINI_HEADER;
import static it.at7.gemini.auth.api.LoginController.LOGIN_PATH;
import static it.at7.gemini.core.RecordConverters.GEMINI_META_FIELD;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class AuthEventsAPIAbstTest extends UnitTestBase {

    @Override
    public boolean initializeSecurity() {
        return true;
    }

    @Test
    public void n1_testAuthEventBefore_onEntityOperationContextCreate() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper(); // for json conversion

        Map<String, Object> json = new HashMap<>();
        json.put("username", AuthModuleRef.USERS.ADMINISTRATOR);
        json.put("password", AuthModuleRef.USERS.ADMINISTRATOR);
        String jsonString = objectMapper.writeValueAsString(json);
        MvcResult result = mockMvc.perform(post(LOGIN_PATH)
                .contentType(APPLICATION_JSON)
                .content(jsonString)
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String stringResponseBody = result.getResponse().getContentAsString();
        Map<String, Object> recordBody = new ObjectMapper().readValue(stringResponseBody,
                new TypeReference<Map<String, Object>>() {
                });
        String access_token = (String) recordBody.get("access_token");

        //==== basic object -- withGeminiSearchString default value
        json = new HashMap<>();
        json.put("text", "lk");
        json.put("numberLong", 10);
        jsonString = objectMapper.writeValueAsString(json);
        result = mockMvc.perform(post(API_PATH + "/TestDataType")
                .header(GEMINI_HEADER, GEMINI_API_META_TYPE)
                .header(AUTHORIZATION, "Bearer" + " " + access_token)
                .contentType(APPLICATION_JSON)
                .content(jsonString)
                .accept(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meta").exists())
                .andReturn();

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> mapResp = mapper.readValue(result.getResponse().getContentAsString(), new TypeReference<Map<String, Object>>() {
        });
        Map<String, Object> meta = (Map<String, Object>) mapResp.get(GEMINI_META_FIELD);
        String createdU = (String) meta.get(AuthMetaRef.FIELDS.CREATED_USER);
        String modifiedU = (String) meta.get(AuthMetaRef.FIELDS.MODIFIED_USER);
        Assert.assertEquals(AuthModuleRef.USERS.ADMINISTRATOR, createdU);
        Assert.assertEquals(AuthModuleRef.USERS.ADMINISTRATOR, modifiedU);
    }
}
