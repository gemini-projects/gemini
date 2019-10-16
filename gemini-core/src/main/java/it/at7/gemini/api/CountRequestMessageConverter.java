package it.at7.gemini.api;

import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class CountRequestMessageConverter extends MappingJackson2HttpMessageConverter {

    @Override
    protected boolean canRead(MediaType mediaType) {
        return false;
    }

    @Override
    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        return super.canWrite(mediaType) && GeminiWrappers.CountRequest.class.isAssignableFrom(clazz);
    }

    @Override
    protected void writeInternal(Object object, Type type, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        GeminiWrappers.CountRequest req = GeminiWrappers.CountRequest.class.cast(object);
        Map<String, Object> res = new HashMap<>();
        res.put("count", req.getResult());
        super.writeInternal(res, type, outputMessage);
    }

}
