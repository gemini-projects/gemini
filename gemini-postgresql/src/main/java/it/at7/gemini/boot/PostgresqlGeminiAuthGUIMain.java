package it.at7.gemini.boot;

import it.at7.gemini.auth.AuthModule;

import java.util.Set;

public class PostgresqlGeminiAuthGUIMain {

    public static void main(String[] args) {
        GeminiPostgresqlMain.startWithGui(args, Set.of(AuthModule.Core.class), Set.of(AuthModule.API.class));
    }
}
