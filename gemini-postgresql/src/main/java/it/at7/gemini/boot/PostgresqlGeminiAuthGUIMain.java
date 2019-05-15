package it.at7.gemini.boot;

import it.at7.gemini.auth.AuthModule;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

@EnableAutoConfiguration
public class PostgresqlGeminiAuthGUIMain {

    public static void main(String[] args) {
        GeminiPostgresqlMain.startWithGui(args, AuthModule.class);
    }
}
