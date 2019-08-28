package it.at7.gemini.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.at7.gemini.UnitTestBase;
import it.at7.gemini.schema.CoreMetaRef;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

import static it.at7.gemini.api.ApiUtility.GEMINI_API_META_TYPE;
import static it.at7.gemini.api.ApiUtility.GEMINI_HEADER;
import static it.at7.gemini.core.RecordConverters.GEMINI_META_FIELD;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class RestApiControllerMultipleFieldLKAbstTest extends UnitTestBase {

    @Test
    public void n1_saveEntity() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper(); // for json conversion

        Map<String, Object> json = new HashMap<>();
        json.put("lk1", "one");
        json.put("lk2", "two");
        String jsonString = objectMapper.writeValueAsString(json);
        mockMvc.perform(post(API_PATH + "/MultipleLKOrdered")
                .contentType(APPLICATION_JSON)
                .content(jsonString)
                .accept(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content()
                        // stric because new data type must fail
                        .json("{'lk1': 'one'; 'lk2':'two'}", true));
        // no duplicated keys
        mockMvc.perform(post(API_PATH + "/MultipleLKOrdered")
                .contentType(APPLICATION_JSON)
                .content(jsonString)
                .accept(APPLICATION_JSON))
                .andExpect(status().is4xxClientError()) // posting an already existent
                .andExpect(content().json("{'errorcode':'MULTIPLE_LK_FOUND'}"));
    }

    @Test
    public void n2_testGetLk() throws Exception {

        mockMvc.perform(get(API_PATH + "/MultipleLKOrdered/one/two")
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content()
                        .json("{'lk1': 'one'; 'lk2':'two'}", true));

        MvcResult result = mockMvc.perform(get(API_PATH + "/MultipleLKOrdered/one/two")
                .header(GEMINI_HEADER, GEMINI_API_META_TYPE)
                .accept(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content()
                        .json("{'data': {'lk1': 'one'; 'lk2':'two'}}"))
                .andExpect(jsonPath("$.meta").exists())
                .andReturn();
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> mapResp = mapper.readValue(result.getResponse().getContentAsString(), new TypeReference<Map<String, Object>>() {
        });
        Map<String, Object> meta = (Map<String, Object>) mapResp.get(GEMINI_META_FIELD);
        String created = (String) meta.get(CoreMetaRef.FIELDS.CREATED);
        String modified = (String) meta.get(CoreMetaRef.FIELDS.MODIFIED);
        Assert.assertNotNull(created);
        Assert.assertNotNull(modified);
        Assert.assertEquals(created, modified);
        Assert.assertEquals(OffsetDateTime.parse(created, ISO_DATE_TIME), OffsetDateTime.parse(modified, ISO_DATE_TIME));
        String inexistent = (String) meta.get("inexistent");
        Assert.assertNull(inexistent);

        mockMvc.perform(get(API_PATH + "/MultipleLKOrdered/two/one")
                .accept(APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void n3_testUpdate() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper(); // for json conversion

        Map<String, Object> json = new HashMap<>();
        json.put("lk2", "one"); // change the lk field -- lk is then one/one
        String jsonString = objectMapper.writeValueAsString(json);
        mockMvc.perform(put(API_PATH + "/MultipleLKOrdered/one/two")
                .contentType(APPLICATION_JSON)
                .content(jsonString)
                .accept(APPLICATION_JSON))
                .andExpect(content()
                        // stric because new data type must fail
                        .json("{'lk1': 'one'; 'lk2':'one'}", true));

        mockMvc.perform(get(API_PATH + "/MultipleLKOrdered/one/two")
                .accept(APPLICATION_JSON))
                .andExpect(status().isNotFound());

        mockMvc.perform(get(API_PATH + "/MultipleLKOrdered/one/one")
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content()
                        .json("{'lk1': 'one'; 'lk2':'one'}", true));
    }

    @Test
    public void n4_testDelete() throws Exception {
        mockMvc.perform(delete(API_PATH + "/MultipleLKOrdered/one/one")
                .accept(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content()
                        .json("{'lk1': 'one'; 'lk2':'one'}", true));
        mockMvc.perform(get(API_PATH + "/MultipleLKOrdered/one/one")
                .accept(APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
