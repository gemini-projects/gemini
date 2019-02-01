package it.at7.gemini.core;

import org.springframework.stereotype.Service;

@Service
@ModuleDescription(
        name = "RUNTIME",
        dependencies = "CORE",
        editable = true)
public class RuntimeModule implements Module {

    @Override
    public String getSchemaResourceLocation() {
        String pattern = "file:./schema/%s.at";
        return String.format(pattern, getName());
    }

    public String getSchemaLocation() {
        String pattern = "./schema/%s.at";
        return String.format(pattern, getName());
    }
}
