package it.at7.gemini.core;

/**
 * A Smart Module can be used to semplify custom modules providing smart schema features. Built on top of {@link GeminiModule}
 * features
 */
public interface SmartModule extends ModuleBase {

    default String getName() {
        Class<? extends SmartModule> classModule = this.getClass();
        return classModule.isAnnotationPresent(SmartModuleDescription.class) ? classModule.getAnnotation(SmartModuleDescription.class).name() : classModule.getName();
    }

    default String[] getDependencies() {
        Class<? extends SmartModule> classModule = this.getClass();
        if (classModule.isAnnotationPresent(SmartModuleDescription.class)) {
            return classModule.getAnnotation(SmartModuleDescription.class).dependencies();
        }
        return new String[]{};
    }

    default String getSchemaResourceLocation() {
        String pattern = "classpath:/smart-schema/%s.yaml";
        return String.format(pattern, getName());
    }

    default int order() {
        Class<? extends SmartModule> classModule = this.getClass();
        if (classModule.isAnnotationPresent(SmartModuleDescription.class)) {
            return classModule.getAnnotation(SmartModuleDescription.class).order();
        }
        return 0;
    }

    default boolean isDynamic() {
        Class<? extends SmartModule> classModule = this.getClass();
        if (classModule.isAnnotationPresent(SmartModuleDescription.class)) {
            return classModule.getAnnotation(SmartModuleDescription.class).dynamic();
        }
        return false;
    }

    default String getSchemaPrefix() {
        Class<? extends SmartModule> classModule = this.getClass();
        if (classModule.isAnnotationPresent(SmartModuleDescription.class)) {
            return classModule.getAnnotation(SmartModuleDescription.class).schemaPrefix();
        }
        return "";
    }

    default String getDEFAULTSchemaResourceLocation() {
        return "classpath:/smart-schema/SMART_SCHEMA_DEFAULT.yaml";
    }
}
