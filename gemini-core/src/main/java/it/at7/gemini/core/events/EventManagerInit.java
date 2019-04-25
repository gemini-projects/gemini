package it.at7.gemini.core.events;

import it.at7.gemini.core.Module;

import java.util.List;

public interface EventManagerInit {
    void loadEvents(List<Module> modulesInOrder);
}
