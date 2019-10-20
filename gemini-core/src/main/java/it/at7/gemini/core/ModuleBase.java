package it.at7.gemini.core;

/**
 * Base interface for Gemini Modules
 */
public interface ModuleBase extends StateListener {

    /**
     * Module name
     *
     * @return
     */
    String getName();

    /**
     * Module dependencies
     *
     * @return
     */
    String[] getDependencies();


    /**
     * Module order
     *
     * @return
     */
    int order();
}
