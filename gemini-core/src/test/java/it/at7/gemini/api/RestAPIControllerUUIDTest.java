package it.at7.gemini.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.at7.gemini.UnitTestBase;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static it.at7.gemini.api.ApiUtility.GEMINI_DATA_TYPE;
import static it.at7.gemini.api.ApiUtility.GEMINI_HEADER;
import static it.at7.gemini.core.RecordConverters.GEMINI_META_FIELD;
import static it.at7.gemini.core.RecordConverters.GEMINI_UUID_FIELD;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class RestAPIControllerUUIDTest extends UnitTestBase {

    private static UUID lkUUID;

    @Test
    public void n1_saveEntity() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper(); // for json conversion

        //==== basic object -- withGeminiSearchString default value
        Map<String, Object> json = new HashMap<>();
        json.put("text", "lk");
        Map<String, Object> geminiAPIJson = new HashMap<>();
        geminiAPIJson.put("data", json);
        geminiAPIJson.put("meta", new HashMap<>());
        String jsonString = objectMapper.writeValueAsString(geminiAPIJson);
        MvcResult result = mockMvc.perform(post(API_PATH + "/TestDataType")
                .header(GEMINI_HEADER, GEMINI_DATA_TYPE)
                .contentType(APPLICATION_JSON)
                .content(jsonString)
                .accept(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content()
                        .json("{'data': {'text':'lk'}}"))
                .andExpect(jsonPath("$.meta").exists())
                .andReturn();

        ObjectMapper mapper = new ObjectMapper();

        Map<String, Object> mapResp = mapper.readValue(result.getResponse().getContentAsString(), new TypeReference<Map<String, Object>>() {
        });
        Map<String, Object> meta = (Map<String, Object>) mapResp.get(GEMINI_META_FIELD);
        String uuid = (String) meta.get(GEMINI_UUID_FIELD);

        lkUUID = UUID.fromString(uuid); // it should not throw exception
    }

    @Test
    public void n2_testGetLk() throws Exception {

        String targetJson = String.format("{'data': {'text':'lk'}, 'meta': {'uuid': %s}}", lkUUID.toString());

        //==== basic object -- withGeminiSearchString default value
        mockMvc.perform(get(API_PATH + "/TestDataType/" + lkUUID)
                .header(GEMINI_HEADER, GEMINI_DATA_TYPE)
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content()
                        .json(targetJson));
    }

    @Test
    public void n3_testUpdate() throws Exception {
        String targetJson = String.format("{'data': {'text':'lk', 'numberLong': 100}, 'meta': {'uuid': %s}}", lkUUID.toString());

        ObjectMapper objectMapper = new ObjectMapper(); // for json conversion

        //==== basic object -- withGeminiSearchString default value
        Map<String, Object> json = new HashMap<>();
        json.put("numberLong", 100);
        Map<String, Object> geminiAPIJson = new HashMap<>();
        geminiAPIJson.put("data", json);
        geminiAPIJson.put("meta", new HashMap<>());
        String jsonString = objectMapper.writeValueAsString(geminiAPIJson);
        mockMvc.perform(put(API_PATH + "/TestDataType/" + lkUUID)
                .header(GEMINI_HEADER, GEMINI_DATA_TYPE)
                .contentType(APPLICATION_JSON)
                .content(jsonString)
                .accept(APPLICATION_JSON))
                .andDo(print())
                .andExpect(content()
                        .json(targetJson));
    }

    @Test
    public void n4_testDelete() throws Exception {

        //==== basic object -- withGeminiSearchString default value
        mockMvc.perform(delete(API_PATH + "/TestDataType/" + lkUUID)
                .accept(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content()
                        .json("{'bool':false,'text':'lk','domain1':{},'numberDouble':0,'numberLong':100, 'date':''}", false)); // TODO need strict true
        mockMvc.perform(get(API_PATH + "/TestDataType/lk")
                .accept(APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
        mockMvc.perform(get(API_PATH + "/TestDataType/" + lkUUID)
                .accept(APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }
}
