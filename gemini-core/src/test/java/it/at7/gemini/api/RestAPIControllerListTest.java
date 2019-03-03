package it.at7.gemini.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.at7.gemini.UnitTestBase;
import it.at7.gemini.core.EntityRecord;
import it.at7.gemini.core.Services;
import it.at7.gemini.core.entitymanager.TestData;
import it.at7.gemini.exceptions.GeminiException;
import org.junit.*;
import org.junit.runners.MethodSorters;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class RestAPIControllerListTest extends UnitTestBase {
/*
    // TODO ADD PERSISTENCE IMPLEMENTATION


    //==== GEMINI TEST PREAMBOLE - WEBAPP APPLICANTION CONTEXT ====/
    static private MockMvc mockMvc;
    static ConfigurableApplicationContext webApp;

    @BeforeClass
    public static void setup() throws SQLException, GeminiException {
        webApp = setupFullWebAPP(RestAPIControllerSingleEntityTest.class);
        mockMvc = webAppContextSetup((WebApplicationContext) webApp).build();
    }

    @AfterClass
    public static void clean() {
        ConfigurableApplicationContext parent = (ConfigurableApplicationContext) webApp.getParent();
        parent.close();
        webApp.close();
    }
    //=============================================================/

    @Test
    public void n1_t() throws Exception {
        // lets save 1000 entity records
        for (int i = 1; i <= 1000; i++) {
            EntityRecord entityRecord = TestData.getTestDataTypeEntityRecord("logKey-" + i);
            Services.getEntityManager().putIfAbsent(entityRecord);
        }
        MvcResult result = mockMvc.perform(get(API_PATH + "/TestDataType")
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        String stringResponseBody = result.getResponse().getContentAsString();
        List<Map<String, Object>> listRecord = new ObjectMapper().readValue(stringResponseBody,
                new TypeReference<List<Map<String, Object>>>() {
                });
        Assert.assertEquals(1000, listRecord.size());
    }

    */


}
