package it.at7.gemini.core;

import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Gemini Raw Module is the entry point to organize schemas and entities.
 */
public interface GeminiModule extends ModuleBase {

    default String getName() {
        Class<? extends GeminiModule> classModule = this.getClass();
        return classModule.isAnnotationPresent(ModuleDescription.class) ? classModule.getAnnotation(ModuleDescription.class).name() : classModule.getName();
    }

    default String[] getDependencies() {
        Class<? extends GeminiModule> classModule = this.getClass();
        if (classModule.isAnnotationPresent(ModuleDescription.class)) {
            return classModule.getAnnotation(ModuleDescription.class).dependencies();
        }
        return new String[]{};
    }

    default boolean editable() {
        Class<? extends GeminiModule> classModule = this.getClass();
        if (classModule.isAnnotationPresent(ModuleDescription.class)) {
            return classModule.getAnnotation(ModuleDescription.class).editable();
        }
        return false;
    }

    default int order() {
        Class<? extends GeminiModule> classModule = this.getClass();
        if (classModule.isAnnotationPresent(ModuleDescription.class)) {
            return classModule.getAnnotation(ModuleDescription.class).order();
        }
        return 0;
    }

    @Nullable
    default String getSchemaResourceLocation() {
        String pattern = "classpath:/schemas/%s.at";
        return String.format(pattern, getName());
    }

    default String getSchemaRecordResourceLocation() {
        String pattern = "classpath:/records/%s.atr";
        return String.format(pattern, getName());
    }

    default String getEntityRecordResourceLocation(String entityName) {
        String pattern = "classpath:/records/%s.atr";
        return String.format(pattern, entityName);
    }

    default boolean createSchemaIfNotFound() {
        return false;
    }

    @Nullable
    default String getSchemaLocation() {
        return getSchemaResourceLocation().replace("classpath: ", "");
    }

    static Collection<GeminiModule> getDependenciesClosure(Map<String, GeminiModule> modules, GeminiModule targetModule) {
        if (targetModule == null) {
            return List.of();
        }

        ArrayList<GeminiModule> res = new ArrayList<>();
        res.add(targetModule);

        String[] dependencies = targetModule.getDependencies();
        for (String dep : dependencies) {
            GeminiModule module = modules.get(dep);
            res.addAll(getDependenciesClosure(modules, module));
        }
        return res;
    }
}
