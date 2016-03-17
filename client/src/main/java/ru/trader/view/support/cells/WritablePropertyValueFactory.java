package ru.trader.view.support.cells;

import com.sun.javafx.property.PropertyReference;
import javafx.beans.NamedArg;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import ru.trader.view.support.PropertyFactory;

public class WritablePropertyValueFactory<S,T> extends PropertyFactory<T, S> implements Callback<TableColumn.CellDataFeatures<S, T>, ObservableValue<T>> {
    public WritablePropertyValueFactory(@NamedArg("property") String property) {
        super(property);
    }


    @Override
    public ObservableValue<T> call(TableColumn.CellDataFeatures<S, T> param) {
        return getCellValue(param.getValue());
    }

    private ObservableValue<T> getCellValue(final S rowData){
        ObservableValue<T> value = null;
        final PropertyReference<T> prop = getPropertyRef(rowData);
        if (prop != null){
            if (prop.hasProperty()) value = prop.getProperty(rowData);
            else if (prop.isWritable()) {
                value = new SimpleObjectProperty<>(prop.get(rowData));
                value.addListener((ov , o, n) -> prop.set(rowData, n));
            } else
                value = new ReadOnlyObjectWrapper<>(prop.get(rowData));
        }
        return value;
    }


}
