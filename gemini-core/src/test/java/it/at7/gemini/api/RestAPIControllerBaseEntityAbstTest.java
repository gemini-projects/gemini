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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
public abstract class RestAPIControllerBaseEntityAbstTest extends UnitTestBase {

    @Test
    public void n1_saveEntity() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper(); // for json conversion

        //==== basic object -- withGeminiSearchString default value
        Map<String, Object> json = new HashMap<>();
        json.put("text", "lk");
        json.put("numberLong", 10);
        String jsonString = objectMapper.writeValueAsString(json);
        mockMvc.perform(post(API_PATH + "/TestDataType")
                .contentType(APPLICATION_JSON)
                .content(jsonString)
                .accept(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content()
                        // stric because new data type must fail
                        .json("{'bool':false,'text':'lk','domain1':{},'numberDouble':0,'double':0.0,'numberLong':10, 'long':0,'date':'', 'time': '', 'datetime':'', 'textArray':[], 'domain1Array':[]}", true));
        // no duplicated keys
        mockMvc.perform(post(API_PATH + "/TestDataType")
                .contentType(APPLICATION_JSON)
                .content(jsonString)
                .accept(APPLICATION_JSON))
                .andExpect(status().is4xxClientError()) // posting an already existent
                .andExpect(content().json("{'errorcode':'MULTIPLE_LK_FOUND'}"));


        //==== basic object --  all basic types value
        Map<String, Object> jsonAllBasicTypes = new HashMap<>();
        jsonAllBasicTypes.put("text", "lk-allBasicTypes");
        jsonAllBasicTypes.put("numberlong", 10);
        jsonAllBasicTypes.put("long", 100);
        jsonAllBasicTypes.put("numberDouble", 11.1);
        jsonAllBasicTypes.put("double", 111.11);
        jsonAllBasicTypes.put("date", "1989/9/6");
        jsonAllBasicTypes.put("time", "02:10");
        jsonAllBasicTypes.put("datetime", "1989-09-06T01:01");
        jsonAllBasicTypes.put("textArray", new String[]{"abc", "def"});
        String jsonAllBtypeString = objectMapper.writeValueAsString(jsonAllBasicTypes);
        mockMvc.perform(post(API_PATH + "/TestDataType")
                .contentType(APPLICATION_JSON)
                .content(jsonAllBtypeString)
                .accept(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content()
                        // stric because new data type must fail
                        .json("{'bool':false,'text':'lk-allBasicTypes','domain1':{},'numberDouble':11.1, 'double': 111.11, 'numberLong':10, 'long': 100, 'date':'1989-09-06', 'time': '02:10:00Z', 'datetime':'1989-09-06T01:01:00Z','textArray': ['abc','def'], 'domain1Array':[]}", true));


        //==== object entity reference (FK) - single logical key
        Map<String, Object> domainJson = new HashMap<>();
        domainJson.put("code", "dm1");
        String domainJsonString = objectMapper.writeValueAsString(domainJson);
        mockMvc.perform(post(API_PATH + "/TestDomain1")
                .contentType(APPLICATION_JSON)
                .content(domainJsonString)
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content()
                        .json("{'code':'dm1'}"));
        Map<String, Object> jsonWithDomain = new HashMap<>();
        jsonWithDomain.put("text", "lkWithDomain");
        jsonWithDomain.put("numberlong", 11);
        jsonWithDomain.put("domain1", "dm1");
        String dtWithDomain = objectMapper.writeValueAsString(jsonWithDomain);
        mockMvc.perform(post(API_PATH + "/TestDataType")
                .contentType(APPLICATION_JSON)
                .content(dtWithDomain)
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content()
                        // stric because new data type must fail
                        .json("{'bool':false,'text':'lkWithDomain','domain1':'dm1','numberDouble':0,'numberLong':11, 'long': 0, 'double': 0.0}"));

        //=== lets POST a list of Objects
        List<Map<String, Object>> objList = new ArrayList<>();
        Map<String, Object> o1 = new HashMap<>();
        o1.put("text", "lk_list");
        o1.put("numberlong", 20);
        objList.add(o1);
        Map<String, Object> o2 = new HashMap<>();
        o2.put("text", "lkWithDomain_list");
        o2.put("numberlong", 22);
        o2.put("domain1", "dm1");
        objList.add(o2);
        String obkListJsonString = objectMapper.writeValueAsString(objList);
        mockMvc.perform(post(API_PATH + "/TestDataType")
                .contentType(APPLICATION_JSON)
                .content(obkListJsonString)
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content()
                        .json("[ {'bool':false,'text':'lk_list','domain1':{},'numberDouble':0,'numberLong':20}, " +
                                "{'bool':false,'text':'lkWithDomain_list','domain1':'dm1','numberDouble':0,'numberLong':22}" +
                                "]"));


        //==== test entity withGeminiSearchString FK of to a hierarchy domain (LK withGeminiSearchString FK)
        Map<String, Object> domainHKJson = new HashMap<>();
        domainHKJson.put("code", "dmH1");
        domainHKJson.put("domain1", "dm1");
        String domainHJsonString = objectMapper.writeValueAsString(domainHKJson);
        mockMvc.perform(post(API_PATH + "/TestDomainHierarchy")
                .contentType(APPLICATION_JSON)
                .content(domainHJsonString)
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content()
                        .json("{'code':'dmH1', 'domain1': 'dm1'}", true));
        Map<String, Object> dtWuthHierarchy = new HashMap<>();
        dtWuthHierarchy.put("text", "lkWithHKDomain");
        dtWuthHierarchy.put("dmHierarchy", domainHKJson);
        objList.add(o2);
        String dtWithHierarchyJSString = objectMapper.writeValueAsString(dtWuthHierarchy);
        mockMvc.perform(post(API_PATH + "/TestDataTypeWithHierachy")
                .contentType(APPLICATION_JSON)
                .content(dtWithHierarchyJSString)
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content()
                        .json("{'text':'lkWithHKDomain','dmHierarchy':{'code':'dmH1', 'domain1': 'dm1'}}'", true));
    }

    @Test
    public void n2_testGetLk() throws Exception {

        //==== basic object -- default value
        mockMvc.perform(get(API_PATH + "/TestDataType/lk")
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content()
                        .json("{'bool':false,'text':'lk','domain1':{},'numberDouble':0,'numberLong':10}"));

        //==== basic object -- default value -- gemini API data type (WITH META Fields)
        MvcResult result = mockMvc.perform(get(API_PATH + "/TestDataType/lk")
                .header(GEMINI_HEADER, GEMINI_API_META_TYPE)
                .accept(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content()
                        .json("{'data': {'bool':false,'text':'lk','domain1':{},'numberDouble':0,'numberLong':10}}"))
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

        //==== basic object -- all basic types value
        mockMvc.perform(get(API_PATH + "/TestDataType/lk-allBasicTypes")
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content()
                        // stric because new data type must fail
                        .json("{'bool':false,'text':'lk-allBasicTypes','domain1':{},'numberDouble':11.1,'double': 111.11, 'numberLong':10, 'long': 100, 'date':'1989-09-06', 'time': '02:10:00Z', 'datetime':'1989-09-06T01:01:00Z', 'textArray': ['abc','def'], 'domain1Array':[]}", true));


        //==== object entity reference (FK) - single logical key
        mockMvc.perform(get(API_PATH + "/TestDataType/lkWithDomain")
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content()
                        .json("{'bool':false,'text':'lkWithDomain','domain1':'dm1','numberDouble':0,'numberLong':11}"));


        //==== test entity  FK of to a hierarchy domain (LK withGeminiSearchString FK)
        mockMvc.perform(get(API_PATH + "/TestDataTypeWithHierachy/lkWithHKDomain")
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content()
                        .json("{'text':'lkWithHKDomain','dmHierarchy':{'code':'dmH1', 'domain1': 'dm1'}}'", true));
    }

    @Test
    public void n3_testUpdate() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper(); // for json conversion

        //==== basic object --  default value
        Map<String, Object> json = new HashMap<>();
        json.put("numberlong", 100);
        String jsonString = objectMapper.writeValueAsString(json);
        mockMvc.perform(put(API_PATH + "/TestDataType/lk")
                .contentType(APPLICATION_JSON)
                .content(jsonString)
                .accept(APPLICATION_JSON))
                .andExpect(content()
                        // stric because new data type must fail
                        .json("{'bool':false,'text':'lk','domain1':{},'numberDouble':0,'numberLong':100, 'date':''}", false));

        //==== basic object --  meta fields - default value
        json = new HashMap<>();
        json.put("numberlong", 150);
        jsonString = objectMapper.writeValueAsString(json);
        MvcResult result = mockMvc.perform(put(API_PATH + "/TestDataType/lk")
                .contentType(APPLICATION_JSON)
                .header(GEMINI_HEADER, GEMINI_API_META_TYPE)
                .content(jsonString)
                .accept(APPLICATION_JSON))
                .andExpect(content()
                        // stric because new data type must fail
                        .json("{'data': {'bool':false,'text':'lk','domain1':{},'numberDouble':0,'numberLong':150, 'date':''}}", false))
                .andReturn();
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> mapResp = mapper.readValue(result.getResponse().getContentAsString(), new TypeReference<Map<String, Object>>() {
        });
        Map<String, Object> meta = (Map<String, Object>) mapResp.get(GEMINI_META_FIELD);
        String created = (String) meta.get(CoreMetaRef.FIELDS.CREATED);
        String modified = (String) meta.get(CoreMetaRef.FIELDS.MODIFIED);
        Assert.assertNotNull(created);
        Assert.assertNotNull(modified);
        Assert.assertNotEquals(created, modified);
        Assert.assertNotEquals(OffsetDateTime.parse(created, ISO_DATE_TIME), OffsetDateTime.parse(modified, ISO_DATE_TIME));

        //==== update a domain and query the object that refers it
        //==== object entity reference (FK) - single logical key
        Map<String, Object> domainJson = new HashMap<>();
        domainJson.put("code", "dm2");
        String domainJsonString = objectMapper.writeValueAsString(domainJson);
        mockMvc.perform(put(API_PATH + "/TestDomain1/dm1")
                .contentType(APPLICATION_JSON)
                .content(domainJsonString)
                .accept(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content()
                        .json("{'code':'dm2'}"));
        //==== object withGeminiSearchString entity reference (FK) - single logical key
        mockMvc.perform(get(API_PATH + "/TestDataType/lkWithDomain")
                .accept(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content()
                        .json("{'bool':false,'text':'lkWithDomain','domain1':'dm2','numberDouble':0,'numberLong':11}"));

    }

    @Test
    public void n4_testDelete() throws Exception {

        //==== basic object -- withGeminiSearchString default value
        mockMvc.perform(delete(API_PATH + "/TestDataType/lk")
                .accept(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content()
                        .json("{'bool':false,'text':'lk','domain1':{},'numberDouble':0,'numberLong':150, 'date':''}", false)); // TODO need strict true
        mockMvc.perform(get(API_PATH + "/TestDataType/lk")
                .accept(APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void n5_testDeleteWithResolution() throws Exception {
        //==== delete a domain and query the object that refers it
        mockMvc.perform(delete(API_PATH + "/TestDomain1/dm2")
                .accept(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content()
                        .json("{'code':'dm2'}"));
        mockMvc.perform(delete(API_PATH + "/TestDomain1/dm2")
                .accept(APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

        //==== object withGeminiSearchString entity reference (FK) - single logical key
        mockMvc.perform(get(API_PATH + "/TestDataType/lkWithDomain")
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content()
                        .json("{'bool':false,'text':'lkWithDomain','domain1':{},'numberDouble':0,'numberLong':11}"));
    }

}