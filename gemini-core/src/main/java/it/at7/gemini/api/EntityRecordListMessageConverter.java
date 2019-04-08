package it.at7.gemini.api;

import it.at7.gemini.core.RecordConverters;
import it.at7.gemini.core.EntityRecord;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EntityRecordListMessageConverter extends MappingJackson2HttpMessageConverter {

    @Override
    protected boolean canRead(MediaType mediaType) {
        return false;
    }

    @Override
    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        return super.canWrite(mediaType) && GeminiWrappers.EntityRecordsList.class.isAssignableFrom(clazz);
    }

    @Override
    protected void writeInternal(Object object, Type type, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        GeminiWrappers.EntityRecordsList recordsWrapper = GeminiWrappers.EntityRecordsList.class.cast(object);
        Collection<EntityRecord> records = recordsWrapper.getRecords();
        List<Object> listOfFields = new ArrayList<>(records.size());
        for (EntityRecord record : records) {
            listOfFields.add(RecordConverters.fieldsToJSONMap(record));
        }
        super.writeInternal(listOfFields, type, outputMessage);
    }
}
