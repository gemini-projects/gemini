package it.at7.gemini.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.at7.gemini.core.EntityRecord;
import it.at7.gemini.core.Services;
import it.at7.gemini.core.entitymanager.TestData;
import it.at7.gemini.schema.CoreMetaRef;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.test.web.servlet.MvcResult;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

import static it.at7.gemini.api.ApiUtility.GEMINI_API_META_TYPE;
import static it.at7.gemini.api.ApiUtility.GEMINI_HEADER;
import static it.at7.gemini.api.MockMVCUtils.API_PATH;
import static it.at7.gemini.api.MockMVCUtils.mockMvc;
import static it.at7.gemini.core.RecordConverters.GEMINI_META_FIELD;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RestAPIControllerGenericEntityRefAbstTest {

    @Test
    public void n1_saveEntity() throws Exception {
        // insert one domain record to be usead as reference
        EntityRecord lk1 = TestData.getDomain_singlelk_EntityRecord("domain_lk1");
        Services.getEntityManager().putOrUpdate(lk1);

        ObjectMapper objectMapper = new ObjectMapper(); // for json conversion


        //==== insert generic entity rec reference with only one logical key
        Map<String, Object> json = new HashMap<>();
        json.put("logKey", "lk");
        Map<String, Object> lkObj = new HashMap<>();
        lkObj.put("entity", lk1.getEntity().getName());
        lkObj.put("ref", "domain_lk1");
        json.put("genericRef", lkObj);
        String jsonString = objectMapper.writeValueAsString(json);
        mockMvc.perform(post(API_PATH + "/WithGenericEntityRef")
                .contentType(APPLICATION_JSON)
                .content(jsonString)
                .accept(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content()
                        // stric because new data type must fail
                        .json("{'logKey': 'lk', 'genericRef': {'entity': 'TESTDOMAIN1', 'ref': 'domain_lk1'}}", true));
        // no duplicated keys
        mockMvc.perform(post(API_PATH + "/WithGenericEntityRef")
                .contentType(APPLICATION_JSON)
                .content(jsonString)
                .accept(APPLICATION_JSON))
                .andExpect(status().is4xxClientError()) // posting an already existent
                .andExpect(content().json("{'errorcode':'MULTIPLE_LK_FOUND'}"));


        //==== insert generic entity rec reference with more than one logical key
        Map<String, Object> jsonMoreLk = new HashMap<>();
        jsonMoreLk.put("lk1", "one");
        jsonMoreLk.put("lk2", "two");
        jsonString = objectMapper.writeValueAsString(jsonMoreLk);
        mockMvc.perform(post(API_PATH + "/MultipleLKOrdered")
                .contentType(APPLICATION_JSON)
                .content(jsonString)
                .accept(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        //==== insert generic entity rec reference with only one logical key

        json.put("logKey", "lk2");
        Map<String, Object> lkObj2 = new HashMap<>();
        lkObj2.put("entity", Services.getEntityManager().getEntity("MultipleLKOrdered").getName().toUpperCase());
        lkObj2.put("ref", jsonMoreLk);
        json.put("genericRef", lkObj2);

        jsonString = objectMapper.writeValueAsString(json);
        mockMvc.perform(post(API_PATH + "/WithGenericEntityRef")
                .contentType(APPLICATION_JSON)
                .content(jsonString)
                .accept(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content()
                        // stric because new data type must fail
                        .json("{'logKey': 'lk2', 'genericRef': {'entity': 'MULTIPLELKORDERED', 'ref': {'lk1': 'one', 'lk2': 'two'}}}", true));
    }

    @Test
    public void n2_testGetLk() throws Exception {
        //==== basic object -- default value
        mockMvc.perform(get(API_PATH + "/WithGenericEntityRef/lk")
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content()
                        .json("{'logKey': 'lk', 'genericRef': {'entity': 'TESTDOMAIN1', 'ref': 'domain_lk1'}}", true));

        //==== basic object -- default value -- gemini API data type (WITH META Fields)
        MvcResult result = mockMvc.perform(get(API_PATH + "/WithGenericEntityRef/lk")
                .header(GEMINI_HEADER, GEMINI_API_META_TYPE)
                .accept(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content()
                        .json("{'data': {'logKey': 'lk', 'genericRef': {'entity': 'TESTDOMAIN1', 'ref': 'domain_lk1'}}}"))
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
    }


    @Test
    public void n3_update() throws Exception {
        EntityRecord lk1 = TestData.getDomain_singlelk_EntityRecord("domain_lk2");
        Services.getEntityManager().putOrUpdate(lk1);

        ObjectMapper objectMapper = new ObjectMapper(); // for json conversion

        //==== udpdate generic entity rec
        Map<String, Object> json = new HashMap<>();
        json.put("logKey", "lk");
        Map<String, Object> lkObj = new HashMap<>();
        lkObj.put("entity", lk1.getEntity().getName());
        lkObj.put("ref", "domain_lk2");
        json.put("genericRef", lkObj);
        String jsonString = objectMapper.writeValueAsString(json);
        mockMvc.perform(put(API_PATH + "/WithGenericEntityRef/lk")
                .contentType(APPLICATION_JSON)
                .content(jsonString)
                .accept(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content()
                        // stric because new data type must fail
                        .json("{'logKey': 'lk', 'genericRef': {'entity': 'TESTDOMAIN1', 'ref': 'domain_lk2'}}", true));
    }

    @Test
    public void n4_testDelete() throws Exception {
        mockMvc.perform(delete(API_PATH + "/WithGenericEntityRef/lk2")
                .accept(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content()
                        .json("{'logKey': 'lk2', 'genericRef': {'entity': 'MULTIPLELKORDERED', 'ref': {'lk1': 'one', 'lk2': 'two'}}}", true));
        mockMvc.perform(get(API_PATH + "/WithGenericEntityRef/lk2")
                .accept(APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }


    @Test
    public void n5_testDeleteWithResolution() throws Exception {
        //==== delete a domain and query the object that refers it
        mockMvc.perform(delete(API_PATH + "/TestDomain1/domain_lk2")
                .accept(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
        mockMvc.perform(get(API_PATH + "/TestDomain1/domain_lk2")
                .accept(APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

        //==== object withGeminiSearchString entity reference (FK) - single logical key
        mockMvc.perform(get(API_PATH + "/WithGenericEntityRef/lk")
                .accept(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content()
                        .json("{'logKey': 'lk', 'genericRef': {}}", true));
    }

}
