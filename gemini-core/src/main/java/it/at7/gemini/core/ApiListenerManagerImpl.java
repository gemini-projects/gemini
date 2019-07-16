package it.at7.gemini.core;

import it.at7.gemini.api.ApiListenerManagerInit;
import it.at7.gemini.api.ApiListenersManager;
import it.at7.gemini.api.RestAPIControllerListener;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class ApiListenerManagerImpl implements ApiListenerManagerInit, ApiListenersManager {

    List<RestAPIControllerListener> apiControllerListeners;

    @Autowired
    public ApiListenerManagerImpl() {
        apiControllerListeners = new ArrayList<>();
    }

    public void registerApiControllerListener(RestAPIControllerListener restAPIControllerListener) {
        apiControllerListeners.add(restAPIControllerListener);
    }

    @NotNull
    public List<RestAPIControllerListener> getApiControllerListeners() {
        return Collections.unmodifiableList(apiControllerListeners);
    }
}
