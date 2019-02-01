package it.at7.gemini.gui.annotation;

import java.util.Optional;

public interface GeminiGuiComponentHook {

    default Optional<Object> onInit() {
        return Optional.empty();
    }
}
