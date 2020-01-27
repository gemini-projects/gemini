package it.at7.gemini.boot;

import it.at7.gemini.auth.AuthModule;
import it.at7.gemini.auth.AuthModuleAPI;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({AuthModule.class, AuthModuleAPI.class})
public class GeminiAuth {
}
