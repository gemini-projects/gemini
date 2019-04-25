package it.at7.gemini.core;

import it.at7.gemini.conf.State;
import org.springframework.stereotype.Service;


@Service
@ModuleDescription(
        name = "CORE",
        dependencies = {},
        order = -700)
public class CoreModule implements Module {
    @Override
    public void onChange(State previous, State actual) {

    }
}
