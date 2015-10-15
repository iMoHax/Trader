package ru.trader.view.support.autocomplete;


import javafx.beans.InvalidationListener;
import javafx.collections.ObservableList;
import org.controlsfx.control.textfield.AutoCompletionBinding;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class CachedSuggestionProvider<T> implements SuggestionProvider<T> {
    private final List<T> cache = new ArrayList<>();
    private final ReentrantLock lock = new ReentrantLock();
    private final AbstractSuggestionProvider<T> provider;
    private AutoCompletionBinding.ISuggestionRequest lastRequest;

    public CachedSuggestionProvider(AbstractSuggestionProvider<T> provider) {
        this.provider = provider;
        provider.getPossibleSuggestions().addListener(listChangeListener);
    }

    @Override
    public ObservableList<T> getPossibleSuggestions() {
        return provider.getPossibleSuggestions();
    }

    @Override
    public void setPossibleSuggestions(ObservableList<T> possibleSuggestions){
        lock.lock();
        try {
            provider.getPossibleSuggestions().removeListener(listChangeListener);
            provider.setPossibleSuggestions(possibleSuggestions);
            cache.clear();
            provider.getPossibleSuggestions().addListener(listChangeListener);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public final Collection<T> call(final AutoCompletionBinding.ISuggestionRequest request) {
        List<T> suggestions = new ArrayList<>();
        if(!request.getUserText().isEmpty()){
            lock.lock();
            try {
                boolean cached = lastRequest != null && isContinue(lastRequest, request);
                if (!cached){
                    cache.clear();
                    cache.addAll(provider.call(request));
                } else {
                    Iterator<T> iterator = cache.iterator();
                    while (iterator.hasNext()) {
                        T possibleSuggestion = iterator.next();
                        if (!provider.isMatch(possibleSuggestion, request)) {
                            iterator.remove();
                        }
                    }
                }
                suggestions.addAll(cache);
                lastRequest = request;
            } finally {
                lock.unlock();
            }
        }
        return suggestions;
    }

    protected boolean isContinue(final AutoCompletionBinding.ISuggestionRequest lastRequest, final AutoCompletionBinding.ISuggestionRequest request){
        String last = lastRequest.getUserText();
        String current = request.getUserText();
        return last != null && current != null && current.toLowerCase().startsWith(last.toLowerCase());
    }

    public void dispose(){
        provider.getPossibleSuggestions().removeListener(listChangeListener);
    }

    private final InvalidationListener listChangeListener = o -> {
        lock.lock();
        try {
            lastRequest = null;
        } finally {
            lock.unlock();
        }
    };
}
