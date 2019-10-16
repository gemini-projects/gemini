package it.at7.gemini.api;

import it.at7.gemini.core.FilterContext;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import static it.at7.gemini.core.FilterContextBuilder.*;

public class CountRequestApiMessageConverter extends MappingJackson2HttpMessageConverter {
    @Override
    protected boolean canRead(MediaType mediaType) {
        return false;
    }

    @Override
    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        return super.canWrite(mediaType) && GeminiWrappers.CountRequestApiType.class.isAssignableFrom(clazz);
    }

    @Override
    protected void writeInternal(Object object, Type type, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        GeminiWrappers.CountRequestApiType record = GeminiWrappers.CountRequestApiType.class.cast(object);
        GeminiWrappers.CountRequest countRequest = record.getCountRequest();// unwrap

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("meta", getMeta(countRequest));
        responseBody.put("data", getData(countRequest));
        super.writeInternal(responseBody, type, outputMessage);
    }

    private Map<String, Object> getMeta(GeminiWrappers.CountRequest countRequest) {
        Map<String, Object> meta = new HashMap<>();
        FilterContext filterContext = countRequest.getFilterContext();
        if (filterContext.getLimit() > 0) {
            meta.put(LIMIT_PARAMETER, filterContext.getLimit());
        }
        if (filterContext.getStart() > 0) {
            meta.put(START_PARAMETER, filterContext.getStart());
        }
        if (filterContext.getOrderBy() != null && filterContext.getOrderBy().length > 0) {
            meta.put(ORDER_BY_PARAMETER, filterContext.getOrderBy());
        }
        return meta;
    }

    private Map<String, Object> getData(GeminiWrappers.CountRequest countRequest) {
        Map<String, Object> res = new HashMap<>();
        res.put("count", countRequest.getResult());
        return res;
    }
}
