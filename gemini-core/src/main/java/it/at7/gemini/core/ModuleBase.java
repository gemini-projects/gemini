package it.at7.gemini.core;

/**
 * Base interface for Gemini Modules
 */
public interface ModuleBase extends StateListener {

    /**
     * Module name
     *
     * @return Module name
     */
    String getName();

    /**
     * Module dependencies
     *
     * @return Module dependencies
     */
    String[] getDependencies();


    /**
     * Module order
     *
     * @return Module order
     */
    int order();
}
