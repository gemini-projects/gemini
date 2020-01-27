package it.at7.gemini.boot.application;

import it.at7.gemini.core.GeminiModule;
import it.at7.gemini.core.ModuleDescription;
import org.springframework.stereotype.Service;

@Service
@ModuleDescription(
        name = "ADMIN-APP",
        dependencies = {},
        order = 10)
public class AdminAppModule implements GeminiModule {
}