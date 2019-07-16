package it.at7.gemini.api;

import java.util.List;

public interface ApiListenersManager {

    List<RestAPIControllerListener> getApiControllerListeners();
}