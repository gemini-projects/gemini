package it.at7.gemini.boot.application.run;

import it.at7.gemini.boot.GeminiAuth;
import it.at7.gemini.boot.GeminiCore;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({GeminiCore.class, GeminiAuth.class})
public class RunCoreAuth {

    public static void main(String[] args) {
        SpringApplication.run(RunCoreAuth.class);
    }
}
