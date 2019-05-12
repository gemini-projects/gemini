package it.at7.gemini;

public abstract class UnitTestNoMockWeb extends UnitTestBase {
    @Override
    public boolean initializeWebApp() {
        return false;
    }
}
