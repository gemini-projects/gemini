package it.at7.gemini.core;

import it.at7.gemini.conf.State;
import it.at7.gemini.exceptions.GeminiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class StateManagerImpl implements StateManager {
    Logger logger = LoggerFactory.getLogger(StateManager.class);

    List<StateListener> listeners = new ArrayList<>();

    private State state = State.STARTING;

    @Override
    public synchronized void changeState(State newState, Optional<Transaction> transaction) throws GeminiException {
        for (StateListener listener : listeners) {
            logger.info("Changing state for {} : {} -> {}", getListenerName(listener), state.name(), newState.name());
            listener.onChange(this.state, newState, transaction);
        }
        logger.info("State Changed: {} -> {}", state.name(), newState.name());
        this.state = newState;
    }

    @Override
    public void register(StateListener listener) {
        listeners.add(listener);
    }

    @Override
    public State getActualState() {
        return state;
    }

    private String getListenerName(StateListener listener) {
        Class<? extends StateListener> cs = listener.getClass();
        return cs.isAnnotationPresent(ModuleDescription.class) ? cs.getAnnotation(ModuleDescription.class).name() : cs.getName();
    }
}
