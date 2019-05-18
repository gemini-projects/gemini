package it.at7.gemini.core;

import org.springframework.stereotype.Service;

@Service
@ModuleDescription(
        name = "IntegrationTest",
        dependencies = {},
        order = 10)
public class IntegrationTestModule implements Module {
}
