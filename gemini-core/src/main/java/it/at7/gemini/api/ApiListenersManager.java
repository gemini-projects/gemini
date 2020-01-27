package it.at7.gemini.api;

import java.util.List;

public interface ApiListenersManager {

    /**
     *
     * @return Get the list of all the API Controller Listeners
     */
    List<RestAPIControllerListener> getApiControllerListeners();
}