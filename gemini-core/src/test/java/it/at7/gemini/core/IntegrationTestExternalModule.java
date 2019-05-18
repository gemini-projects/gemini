package it.at7.gemini.core;

import org.springframework.stereotype.Service;

/**
 * External module is used to add external field to IntegrationTest Entities
 */
@Service
@ModuleDescription(
        name = "IntegrationTestExternal",
        dependencies = {"IntegrationTest"},
        order = 100)
public class IntegrationTestExternalModule implements Module {
}
