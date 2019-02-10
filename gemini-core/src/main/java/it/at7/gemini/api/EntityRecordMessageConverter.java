package it.at7.gemini.api;

import it.at7.gemini.core.RecordConverters;
import it.at7.gemini.core.EntityRecord;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

public class EntityRecordMessageConverter extends MappingJackson2HttpMessageConverter {

    @Override
    protected boolean canRead(MediaType mediaType) {
        return false;
    }

    @Override
    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        return super.canWrite(mediaType) && EntityRecord.class.isAssignableFrom(clazz);
    }

    @Override
    protected void writeInternal(Object object, Type type, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        EntityRecord record = EntityRecord.class.cast(object);
        Map<String, Object> recordMap = RecordConverters.toJSONMap(record);
        super.writeInternal(recordMap, type, outputMessage);
    }
}
