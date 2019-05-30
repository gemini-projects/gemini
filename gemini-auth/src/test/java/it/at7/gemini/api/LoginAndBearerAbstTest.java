package it.at7.gemini.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.at7.gemini.UnitTestBase;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

import static it.at7.gemini.auth.api.LoginController.LOGIN_PATH;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class LoginAndBearerAbstTest extends UnitTestBase {

    static String access_token;

    @Override
    public boolean initializeSecurity() {
        return true;
    }

    @Test
    public void n1_401login() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper(); // for json conversion

        Map<String, Object> json = new HashMap<>();
        json.put("user", "inexistent");
        json.put("password", "inexistent");
        String jsonString = objectMapper.writeValueAsString(json);
        mockMvc.perform(post(LOGIN_PATH)
                .contentType(APPLICATION_JSON)
                .content(jsonString)
                .accept(APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void n2_loginOK() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper(); // for json conversion

        Map<String, Object> json = new HashMap<>();
        json.put("username", "Admin");
        json.put("password", "Admin");
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
        access_token = (String) recordBody.get("access_token");
        Assert.isTrue(access_token.length() > 0, "Access Token should return");
    }

    @Test
    public void n3_getRequestWithToken() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper(); // for json conversion


        Map<String, Object> json = new HashMap<>();
        json.put("text", "lk");
        json.put("numberLong", 10);
        String jsonString = objectMapper.writeValueAsString(json);
        mockMvc.perform(post(API_PATH + "/TestDataType")
                .header(AUTHORIZATION, "Bearer" + " " + access_token)
                .contentType(APPLICATION_JSON)
                .content(jsonString)
                .accept(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());


        //==== basic object -- default value
        mockMvc.perform(get(API_PATH + "/TestDataType/lk")
                .header(AUTHORIZATION, "Bearer" + " " + access_token)
                .accept(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content()
                        .json("{'bool':false,'text':'lk','domain1':{},'numberDouble':0,'numberLong':10}"));


    }


}
