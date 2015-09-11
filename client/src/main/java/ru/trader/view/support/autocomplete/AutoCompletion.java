package ru.trader.view.support.autocomplete;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
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

    private final ObjectProperty<T> completion = new SimpleObjectProperty<>();
    private final StringConverter<T> converter;
    private final AutoTextFieldBinding binding;
    private final T notFoundItem;

    public AutoCompletion(final TextField textField, final Callback<AutoCompletionBinding.ISuggestionRequest, Collection<T>> suggestionProvider) {
        this(textField, suggestionProvider, null, defaultStringConverter());
    }

    public AutoCompletion(final TextField textField, final Callback<AutoCompletionBinding.ISuggestionRequest, Collection<T>> suggestionProvider, T notFoundItem, final StringConverter<T> converter) {
        this.converter = converter;
        this.notFoundItem = notFoundItem;
        binding = new AutoTextFieldBinding(textField, suggestionProvider);
        binding.setOnAutoCompleted(e -> completion.setValue(e.getCompletion()));
    }

    public AutoCompletionBinding<T> getBinding() {
        return binding;
    }

    public T getCompletion() {
        return completion.get();
    }

    public TextField getCompletionTarget(){
        return binding.getCompletionTarget();
    }

    public ObjectProperty<T> completionProperty() {
        return completion;
    }

    public void dispose(){
        binding.dispose();
    }

    public void setValue(T value) {
        completion.setValue(value);
        binding.completeUserInput(value);
    }

    private class AutoTextFieldBinding  extends AutoCompletionBinding<T>{
        public AutoTextFieldBinding(final TextField textField,
                                              Callback<ISuggestionRequest, Collection<T>> suggestionProvider) {

            super(textField, suggestionProvider, converter);

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
        @Override protected void completeUserInput(T completion){
            String newText = converter.toString(completion);
            getCompletionTarget().setText(newText);
            getCompletionTarget().positionCaret(newText.length());
        }

        private final ChangeListener<String> textChangeListener = (obs, oldText, newText) -> {
            if (getCompletionTarget().isFocused()) {
                setUserInput(newText);
                completion.setValue(notFoundItem);
            }
        };

        private final ChangeListener<Boolean> focusChangedListener = (obs, oldFocused, newFocused) -> {
            if(newFocused == false)
                hidePopup();
        };
    }
}
