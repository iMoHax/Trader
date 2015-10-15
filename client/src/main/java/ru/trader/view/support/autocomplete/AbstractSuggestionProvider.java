package ru.trader.view.support.autocomplete;


import javafx.collections.ObservableList;
import org.controlsfx.control.textfield.AutoCompletionBinding;

import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractSuggestionProvider<T> implements SuggestionProvider<T> {
    private ObservableList<T> possibleSuggestions;

    protected AbstractSuggestionProvider(ObservableList<T> possibleSuggestions) {
        this.possibleSuggestions = possibleSuggestions;
    }

    @Override
    public ObservableList<T> getPossibleSuggestions() {
        return possibleSuggestions;
    }

    @Override
    public void setPossibleSuggestions(ObservableList<T> possibleSuggestions){
        this.possibleSuggestions = possibleSuggestions;
    }

    @Override
    public final Collection<T> call(final AutoCompletionBinding.ISuggestionRequest request) {
        List<T> suggestions = new ArrayList<>();
        if(!request.getUserText().isEmpty()){
            suggestions = possibleSuggestions.stream().filter(s -> isMatch(s, request))
                        .sorted(getComparator())
                        .collect(Collectors.toList());
        }
        return suggestions;
    }

    protected abstract Comparator<T> getComparator();
    protected abstract boolean isMatch(T suggestion, AutoCompletionBinding.ISuggestionRequest request);

}
