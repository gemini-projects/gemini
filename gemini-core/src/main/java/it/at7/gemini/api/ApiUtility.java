package it.at7.gemini.api;

import it.at7.gemini.core.EntityRecord;
import it.at7.gemini.exceptions.InvalidRequesException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

public class ApiUtility {
    public static final String GEMINI_HEADER = "Gemini";
    public static final String GEMINI_SIMPLE_DATA_TYPE = "api.nometa";
    public static final String GEMINI_API_META_TYPE = "api.meta";
    public static final String GEMINI_CONTENT_TYPE = "gemini";

    public static boolean noGeminiDataType(List<String> geminiHeader, String acceptType) {
        // first of all check the content type
        if (acceptType.contains(String.format("%s=%s", GEMINI_CONTENT_TYPE, GEMINI_API_META_TYPE))) {
            return false;
        }

        if (geminiHeader == null || geminiHeader.isEmpty() || geminiHeader.contains(GEMINI_SIMPLE_DATA_TYPE)) {
            return true;
        }
        return false;
    }

    public static Object handleGeminiDataTypeResponse(Object results, HttpServletRequest request, HttpServletResponse response) throws InvalidRequesException {
        response.setHeader(GEMINI_HEADER, GEMINI_API_META_TYPE);
        if (results instanceof EntityRecord) {
            return GeminiWrappers.EntityRecordApiType.of((EntityRecord) results);
        }
        if (results instanceof GeminiWrappers.EntityRecordsList) {
            return GeminiWrappers.EntityRecordListApiType.of((GeminiWrappers.EntityRecordsList) results);
        }
        if (results instanceof GeminiWrappers.CountRequest) {
            return GeminiWrappers.CountRequestApiType.of((GeminiWrappers.CountRequest) results);
        }
        throw InvalidRequesException.CANNOT_HANDLE_REQUEST();
    }

    public static boolean geminiDataType(List<String> geminiHeader) {
        return geminiHeader != null && !geminiHeader.isEmpty() && geminiHeader.contains(GEMINI_API_META_TYPE);
    }
}
