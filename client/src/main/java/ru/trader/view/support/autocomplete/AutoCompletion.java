package ru.trader.view.support.autocomplete;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.TextField;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.controlsfx.control.textfield.AutoCompletionBinding;

import java.util.Collection;

public class AutoCompletion<T> {
    private static <T> StringConverter<T> defaultStringConverter() {
        return new StringConverter<T>() {
            @Override public String toString(T t) {
                return t == null ? null : t.toString();
            }
            @SuppressWarnings("unchecked")
            @Override public T fromString(String string) {
                return (T) string;
            }
        };
    }

    private final SuggestionProvider<String> suggestionProvider;
    private final ObjectProperty<T> completion = new SimpleObjectProperty<>();
    private final AutoTextFieldBinding binding;
    private final T notFoundItem;
    private StringConverter<T> converter;

    public AutoCompletion(final TextField textField, final SuggestionProvider<String> suggestionProvider) {
        this(textField, suggestionProvider, null, defaultStringConverter());
    }

    public AutoCompletion(final TextField textField, final SuggestionProvider<String> suggestionProvider, T notFoundItem, final StringConverter<T> converter) {
        this.converter = converter;
        this.notFoundItem = notFoundItem;
        this.suggestionProvider = suggestionProvider;
        binding = new AutoTextFieldBinding(textField, suggestionProvider);
        binding.setOnAutoCompleted(e -> completion.setValue(converter.fromString(e.getCompletion())));
    }

    public AutoCompletionBinding<String> getBinding() {
        return binding;
    }

    public T getValue() {
        return completion.get();
    }

    public TextField getCompletionTarget(){
        return binding.getCompletionTarget();
    }

    public ObjectProperty<T> valueProperty() {
        return completion;
    }

    public void dispose(){
        binding.dispose();
    }

    public void setValue(T value) {
        completion.setValue(value);
        binding.completeUserInput(converter.toString(value));
    }

    public void setConverter(StringConverter<T> converter){
        this.converter = converter;
    }

    public void setSuggestions(ObservableList<String> suggestions){
        suggestionProvider.setPossibleSuggestions(suggestions);
    }

    private class AutoTextFieldBinding  extends AutoCompletionBinding<String>{
        public AutoTextFieldBinding(final TextField textField,
                                              Callback<ISuggestionRequest, Collection<String>> suggestionProvider) {

            super(textField, suggestionProvider, defaultStringConverter());

            getCompletionTarget().textProperty().addListener(textChangeListener);
            getCompletionTarget().focusedProperty().addListener(focusChangedListener);
        }

        /** {@inheritDoc} */
        @Override public TextField getCompletionTarget(){
            return (TextField)super.getCompletionTarget();
        }

        /** {@inheritDoc} */
        @Override public void dispose(){
            getCompletionTarget().textProperty().removeListener(textChangeListener);
            getCompletionTarget().focusedProperty().removeListener(focusChangedListener);
        }

        /** {@inheritDoc} */
        @Override protected void completeUserInput(String completion){
            getCompletionTarget().setText(completion);
            getCompletionTarget().positionCaret(completion.length());
        }

        private final ChangeListener<String> textChangeListener = (obs, oldText, newText) -> {
            if (getCompletionTarget().isFocused()) {
                setUserInput(newText);
                completion.setValue(notFoundItem);
            }
        };

        private final ChangeListener<Boolean> focusChangedListener = (obs, oldFocused, newFocused) -> {
            if(newFocused != null && !newFocused)
                hidePopup();
        };
    }
}
