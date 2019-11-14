package it.at7.gemini.gui.annotation;

import org.springframework.lang.Nullable;

import java.util.Map;
import java.util.Optional;

public interface GeminiGuiComponentHook {

    default Optional<Object> onInit(@Nullable Map<String, Object> body) {
        return Optional.empty();
    }
}
