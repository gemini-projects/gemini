package it.at7.gemini.boot.application.run;

import it.at7.gemini.boot.application.AdminAppModule;
import it.at7.gemini.boot.GeminiPostgresql;

import java.util.Set;

public class FullWithGUI {
    public static void main(String[] args) {
        GeminiPostgresql.start(args, Set.of(AdminAppModule.class), Set.of());
    }
}
