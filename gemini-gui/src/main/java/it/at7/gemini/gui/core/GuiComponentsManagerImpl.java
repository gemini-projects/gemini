package it.at7.gemini.gui.core;

import it.at7.gemini.gui.annotation.GeminiGuiComponent;
import it.at7.gemini.gui.annotation.GeminiGuiComponentHook;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GuiComponentsManagerImpl implements GuiComponentsManager, InitializingBean {

    private ApplicationContext applicationContext;
    Map<String, GeminiGuiComponentHook> guiComponentsBean;

    @Autowired
    public GuiComponentsManagerImpl(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // beans annotated as gemini componenent that implements the common interface
        guiComponentsBean = applicationContext.getBeansOfType(GeminiGuiComponentHook.class)
                .entrySet().stream().filter(e -> e.getValue().getClass().isAnnotationPresent(GeminiGuiComponent.class))
                .collect((Collectors.toMap(e -> e.getKey().toLowerCase(), Map.Entry::getValue)));

        // TODO CHECK duplicated names


    }


    @Override
    public Optional<GeminiGuiComponentHook> getHook(String componentName) {
        return Optional.ofNullable(guiComponentsBean.get(componentName.toLowerCase()));
    }
}
