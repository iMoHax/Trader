package ru.trader.view.support.autocomplete;


import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.util.Callback;
import org.controlsfx.control.textfield.AutoCompletionBinding;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public abstract class CachedSuggestionProvider<T> implements Callback<AutoCompletionBinding.ISuggestionRequest, Collection<T>> {
    private final List<T> cache = new ArrayList<>();
    private final ReentrantLock lock = new ReentrantLock();
    private ObservableList<T> possibleSuggestions;
    private AutoCompletionBinding.ISuggestionRequest lastRequest;

    protected CachedSuggestionProvider(ObservableList<T> possibleSuggestions) {
        this.possibleSuggestions = possibleSuggestions;
        possibleSuggestions.addListener(listChangeListener);
    }

    public void setPossibleSuggestions(ObservableList<T> possibleSuggestions){
        this.possibleSuggestions.removeListener(listChangeListener);
        this.possibleSuggestions = possibleSuggestions;
        cache.clear();
        this.possibleSuggestions.addListener(listChangeListener);
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
                    for (T possibleSuggestion : possibleSuggestions) {
                        if (isMatch(possibleSuggestion, request)) {
                            cache.add(possibleSuggestion);
                        }
                    }
                    Collections.sort(cache, getComparator());
                } else {
                    Iterator<T> iterator = cache.iterator();
                    while (iterator.hasNext()) {
                        T possibleSuggestion = iterator.next();
                        if (!isMatch(possibleSuggestion, request)) {
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

    protected abstract Comparator<T> getComparator();
    protected abstract boolean isMatch(T suggestion, AutoCompletionBinding.ISuggestionRequest request);

    public void dispose(){
        possibleSuggestions.removeListener(listChangeListener);
    }

    private final ListChangeListener<T> listChangeListener = c -> {
        lock.lock();
        try {
            lastRequest = null;
        } finally {
            lock.unlock();
        }
    };
}
