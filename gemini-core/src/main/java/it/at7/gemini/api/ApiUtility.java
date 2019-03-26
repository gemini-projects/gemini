package it.at7.gemini.api;

import java.util.List;

public class ApiUtility {
    public static final String GEMINI_SIMPLE_DATA_TYPE = "gemini.api.nometa";
    public static final String GEMINI_DATA_TYPE = "gemini.api";

    public static boolean noGeminiDataType(List<String> geminiHeader) {
        if (geminiHeader == null || geminiHeader.isEmpty() || geminiHeader.contains(GEMINI_SIMPLE_DATA_TYPE)) {
            return true;
        }
        return false;
    }

    public static boolean geminiDataType(List<String> geminiHeader) {
        return geminiHeader != null && !geminiHeader.isEmpty() && geminiHeader.contains(GEMINI_DATA_TYPE);
    }
}
