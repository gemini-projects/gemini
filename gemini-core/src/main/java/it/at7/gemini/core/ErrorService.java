package it.at7.gemini.core;

import it.at7.gemini.exceptions.GeminiException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class ErrorService implements InitializingBean {
    private List<String> ERROR_CODES = new ArrayList<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AssignableTypeFilter(GeminiException.class));
        Set<BeanDefinition> components = provider.findCandidateComponents("it.at7.gemini");
        for (BeanDefinition component : components) {
            Class<?> gemException = Class.forName(component.getBeanClassName());
            Class<?>[] innerClasses = gemException.getDeclaredClasses();
            for (Class<?> innerClass : innerClasses) {
                String simpleName = innerClass.getSimpleName();
                if (simpleName.equals("Code")) {
                    Enum[] enumConstants = (Enum[]) innerClass.getEnumConstants();
                    register(enumConstants);
                }
            }
        }
    }

    private void register(Enum<?>... errors) {
        for (Enum<?> error : errors) {
            String erroCode = error.name();
            addToErrorCodes(erroCode);
        }
    }

    private void register(String... errors) {
        for (String error : errors) {
            addToErrorCodes(error);
        }
    }

    private void addToErrorCodes(String error) {
        error = error.toUpperCase();
        if (ERROR_CODES.contains(error)) {
            throw new RuntimeException(String.format("DUPLICATE ERROR MESSAGES %s", error));
        }
        ERROR_CODES.add(error);
    }
}
