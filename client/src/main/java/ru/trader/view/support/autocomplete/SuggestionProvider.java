package ru.trader.view.support.autocomplete;

import javafx.collections.ObservableList;
import javafx.util.Callback;
import org.controlsfx.control.textfield.AutoCompletionBinding;

import java.util.Collection;


public interface SuggestionProvider<T> extends Callback<AutoCompletionBinding.ISuggestionRequest, Collection<T>> {
    ObservableList<T> getPossibleSuggestions();

    void setPossibleSuggestions(ObservableList<T> possibleSuggestions);
}
