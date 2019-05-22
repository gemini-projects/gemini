package it.at7.gemini.core;

import org.springframework.stereotype.Service;

@Service
@ModuleDescription(
        name = "CORE",
        dependencies = {},
        order = -700)
public class CoreModule implements Module {
}
