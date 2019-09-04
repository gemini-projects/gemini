package it.at7.gemini.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.at7.gemini.UnitTestBase;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class ClosedDomainRESTAPIControllerAbstTest extends UnitTestBase {

    @Test
    public void n1_postIsNotAllowed() throws Exception {
        mockMvc.perform(post(API_PATH + "/Closed_Domain")
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().is5xxServerError());
    }

    @Test
    public void n2_putIsNotAllowed() throws Exception {
        mockMvc.perform(put(API_PATH + "/Closed_Domain")
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().is5xxServerError());
    }

    @Test
    public void n3_deleteIsNotAllowed() throws Exception {
        mockMvc.perform(delete(API_PATH + "/Closed_Domain")
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().is5xxServerError());
    }

    @Test
    public void n4_getAllDomainRecords() throws Exception {
        MvcResult result = mockMvc.perform(get(API_PATH + "/Closed_Domain?orderBy=code")
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        String stringResponseBody = result.getResponse().getContentAsString();
        List<Map<String, Object>> listRecord = new ObjectMapper().readValue(stringResponseBody,
                new TypeReference<List<Map<String, Object>>>() {
                });
        Assert.assertEquals(2, listRecord.size());
        Map<String, Object> v1 = listRecord.get(0);
        Map<String, Object> v2 = listRecord.get(1);
        Assert.assertEquals("VALUE_1", v1.get("code"));
        Assert.assertEquals("VALUE_2", v2.get("code"));
    }

}
