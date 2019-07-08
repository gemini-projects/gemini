package it.at7.gemini.boot;

import it.at7.gemini.auth.api.AuthModuleAPI;
import it.at7.gemini.auth.core.AuthModule;

import java.util.Set;

public class PostgresqlGeminiAuthGUIMain {

    public static void main(String[] args) {
        GeminiPostgresqlMain.startWithGui(args, Set.of(AuthModule.class), Set.of(AuthModuleAPI.class));
    }
}
