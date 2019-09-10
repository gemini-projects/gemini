package it.at7.gemini.api;

import org.springframework.test.web.servlet.MockMvc;

public class MockMVCUtils {

    /**
     * MVC mock to use inside tests
     * <p>
     * Initialized by persistence driver
     */
    public static MockMvc mockMvc;


    public static String API_PATH = "/api";
}
