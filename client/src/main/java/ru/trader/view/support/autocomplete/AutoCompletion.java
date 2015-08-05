package ru.trader.view.support.autocomplete;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TextField;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;

import java.util.Collection;

public class AutoCompletion<T> {
    private final ObjectProperty<T> completion = new SimpleObjectProperty<>();
    private final StringConverter<T> converter;
    private final AutoCompletionBinding<T> binding;

    public AutoCompletion(final TextField textField, final Callback<AutoCompletionBinding.ISuggestionRequest, Collection<T>> suggestionProvider, final StringConverter<T> converter) {
        this.converter = converter;
        binding = TextFields.bindAutoCompletion(textField, suggestionProvider, converter);
        binding.setOnAutoCompleted(e -> completion.setValue(e.getCompletion()));
    }

    public AutoCompletionBinding<T> getBinding() {
        return binding;
    }

    public T getCompletion() {
        return completion.get();
    }

    public ObjectProperty<T> completionProperty() {
        return completion;
    }

    public void dispose(){
        binding.dispose();
    }

    public void setValue(T value) {
        completion.setValue(value);
        ((TextField)binding.getCompletionTarget()).setText(converter.toString(value));
    }
}
