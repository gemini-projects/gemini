package it.at7.gemini.boot;

import it.at7.gemini.core.Gemini;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.annotation.PostConstruct;

@Configuration
@Import({Gemini.class})
public class GeminiCore {

    @Autowired
    private Gemini gemini;

    @PostConstruct
    public void init() {
        gemini.init();
    }
}
