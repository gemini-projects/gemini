package it.at7.gemini.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.at7.gemini.UnitTestBase;
import it.at7.gemini.core.entitymanager.OneRecordEntityManagerAbstTest;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class RestAPIControllerOneRecordEntityAbstTest extends UnitTestBase {

    @Test
    public void n1_postShouldReturnError() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper(); // for json conversion

        Map<String, Object> json = new HashMap<>();
        json.put("text", "lk");
        String jsonString = objectMapper.writeValueAsString(json);
        mockMvc.perform(post(API_PATH + "/" + OneRecordEntityManagerAbstTest.SINGLETON_ENTITY)
                .contentType(APPLICATION_JSON)
                .content(jsonString)
                .accept(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is5xxServerError());
    }

    @Test
    public void n2_putShouldModifyTherecord() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper(); // for json conversion

        Map<String, Object> json = new HashMap<>();
        json.put("text", "lk");
        String jsonString = objectMapper.writeValueAsString(json);
        mockMvc.perform(put(API_PATH + "/" + OneRecordEntityManagerAbstTest.SINGLETON_ENTITY)
                .contentType(APPLICATION_JSON)
                .content(jsonString)
                .accept(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content()
                        // stric because new data type must fail
                        .json("{'text':'lk'}"));
    }

    @Test
    public void n3_testGetOneRecordEntity() throws Exception {
        //==== basic object -- default value
        mockMvc.perform(get(API_PATH + "/" + OneRecordEntityManagerAbstTest.SINGLETON_ENTITY)
                .accept(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content()
                        .json("{'text':'lk'}"));
    }

    @Test
    public void n4_deleteShouldReturnError() throws Exception {
        mockMvc.perform(delete(API_PATH + "/" + OneRecordEntityManagerAbstTest.SINGLETON_ENTITY)
                .accept(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is5xxServerError());
    }
}
