package it.at7.gemini.boot.application.sync;


import it.at7.gemini.boot.GeminiPostgresql;

import java.util.Set;

public class SyncDatabase {
    public static void main(String[] args) {
        GeminiPostgresql.syncRootContext(args, Set.of());
    }
}
