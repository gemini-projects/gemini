package it.at7.gemini.gui.core;

import it.at7.gemini.gui.annotation.GeminiGuiComponentHook;

import java.util.Optional;

public interface GuiComponentsManager {

    Optional<GeminiGuiComponentHook> getHook(String componentName);

}
