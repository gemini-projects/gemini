package it.at7.gemini.auth;

import it.at7.gemini.core.Module;
import it.at7.gemini.core.ModuleDescription;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;

@Service
@ComponentScan({"it.at7.gemini.auth"})
@ModuleDescription(
        name = "AUTH",
        dependencies = {"CORE"},
        order = -607)
public class AuthModule implements Module {
}
