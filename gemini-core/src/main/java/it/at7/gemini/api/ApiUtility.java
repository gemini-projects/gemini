package it.at7.gemini.api;

import java.util.List;

public class ApiUtility {
    public static final String GEMINI_HEADER = "Gemini";
    public static final String GEMINI_SIMPLE_DATA_TYPE = "api.nometa";
    public static final String GEMINI_API_META_TYPE = "api.meta";
    public static final String GEMINI_CONTENT_TYPE = "gemini";

    public static boolean noGeminiDataType(List<String> geminiHeader, String acceptType) {
        // first of all check the conte type
        if (acceptType.contains(String.format("%s=%s", GEMINI_CONTENT_TYPE, GEMINI_API_META_TYPE))) {
            return false;
        }

        if (geminiHeader == null || geminiHeader.isEmpty() || geminiHeader.contains(GEMINI_SIMPLE_DATA_TYPE)) {
            return true;
        }
        return false;
    }

    public static boolean geminiDataType(List<String> geminiHeader) {
        return geminiHeader != null && !geminiHeader.isEmpty() && geminiHeader.contains(GEMINI_API_META_TYPE);
    }
}
