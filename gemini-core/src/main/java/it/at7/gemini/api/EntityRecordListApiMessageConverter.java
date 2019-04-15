package it.at7.gemini.api;

import it.at7.gemini.core.EntityRecord;
import it.at7.gemini.core.FilterContext;
import it.at7.gemini.core.RecordConverters;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

import static it.at7.gemini.core.FilterContextBuilder.LIMIT_PARAMETER;

public class EntityRecordListApiMessageConverter extends MappingJackson2HttpMessageConverter {

    @Override
    protected boolean canRead(MediaType mediaType) {
        return false;
    }

    @Override
    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        return super.canWrite(mediaType) && GeminiWrappers.EntityRecordListApiType.class.isAssignableFrom(clazz);
    }

    @Override
    protected void writeInternal(Object object, Type type, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        GeminiWrappers.EntityRecordListApiType record = GeminiWrappers.EntityRecordListApiType.class.cast(object);
        GeminiWrappers.EntityRecordsList entityRecordList = record.getEntityRecordList(); // unwrap

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("meta", getMeta(entityRecordList));
        responseBody.put("data", getData(entityRecordList));
        super.writeInternal(responseBody, type, outputMessage);
    }

    private Map<String, Object> getMeta(GeminiWrappers.EntityRecordsList record) {
        Map<String, Object> meta = new HashMap<>();
        FilterContext filterContext = record.getFilterContext();
        if (filterContext.getLimit() > 0) {
            meta.put(LIMIT_PARAMETER, filterContext.getLimit());
        }
        return meta;
    }

    private List<Map<String, Object>> getData(GeminiWrappers.EntityRecordsList entityRecordList) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        for (EntityRecord eRec : entityRecordList.getRecords()) {
            dataList.add(EntityRecordApiTypeMessageConverter.createGeminiApiEntityRecordMap(eRec));
        }
        return dataList;
    }
}
