package it.at7.gemini.core;

public interface Module extends StateListener {

    default String getName() {
        Class<? extends Module> classModule = this.getClass();
        return classModule.isAnnotationPresent(ModuleDescription.class) ? classModule.getAnnotation(ModuleDescription.class).name() : classModule.getName();
    }

    default String[] getDependencies() {
        Class<? extends Module> classModule = this.getClass();
        if (classModule.isAnnotationPresent(ModuleDescription.class)) {
            return classModule.getAnnotation(ModuleDescription.class).dependencies();
        }
        return new String[]{};
    }

    default boolean editable() {
        Class<? extends Module> classModule = this.getClass();
        if (classModule.isAnnotationPresent(ModuleDescription.class)) {
            return classModule.getAnnotation(ModuleDescription.class).editable();
        }
        return false;
    }

    default int order() {
        Class<? extends Module> classModule = this.getClass();
        if (classModule.isAnnotationPresent(ModuleDescription.class)) {
            return classModule.getAnnotation(ModuleDescription.class).order();
        }
        return 0;
    }

    default String getSchemaResourceLocation() {
        String pattern = "classpath:/schemas/%s.at";
        return String.format(pattern, getName());
    }

    default String getSchemaRecordResourceLocation() {
        String pattern = "/records/%s.atr";
        return String.format(pattern, getName());
    }

    default String getEntityRecordResourceLocation(String entityName) {
        String pattern = "/records/%s.atr";
        return String.format(pattern, entityName);
    }

    default boolean createSchemaIfNotFound() {
        return false;
    }

    default String getSchemaLocation() {
        return getSchemaResourceLocation().replace("classpath: ", "");
    }
}
