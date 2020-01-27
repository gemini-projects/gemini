package it.at7.gemini.boot;

import it.at7.gemini.auth.AuthModule;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

public class GeminiModels {

    @Configuration
    @Import({GeminiCore.class, AuthModule.class})
    public static class CoreAuth {
    }
}
