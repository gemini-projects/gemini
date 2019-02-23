package it.at7.gemini.api;

import it.at7.gemini.core.EntityRecord;
import it.at7.gemini.core.RecordConverters;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

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

        GeminiWrappers.EntityRecordsList entityRecordList = record.getEntityRecordList();

        Map<String, Object> results = new HashMap<>();
        results.put("meta", "__TODOOOO___meta_data_here____from entity Record List");

        List<Map<String, Object>> dataList = new ArrayList<>();
        //entityRecordList.getRecords();
        for (EntityRecord eRec : entityRecordList.getRecords()) {
            dataList.add(EntityRecordApiTypeMessageConverter.createGeminiApiEntityRecordMap(eRec));
        }
        results.put("data", dataList);
        super.writeInternal(results, type, outputMessage);

    }
}
