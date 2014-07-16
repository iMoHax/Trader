package ru.trader.view.support;

import com.sun.javafx.property.PropertyReference;
import javafx.beans.NamedArg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertyFactory <S,T> {
    private final static Logger LOG = LoggerFactory.getLogger(PropertyFactory.class);

    private final String property;

    private Class dataClass;
    private String previousProperty;
    protected PropertyReference<S> propertyRef;

    /**
     * Creates a default PropertyValueFactory to extract the value from a given
     * TableView row item reflectively, using the given property name.
     *
     * @param property The name of the property with which to attempt to
     *      reflectively extract a corresponding value for in a given object.
     */
    public PropertyFactory(@NamedArg("property") String property) {
        this.property = property;
    }


    /**
     * Returns the property name provided in the constructor.
     */
    public final String getProperty() { return property; }

    public final PropertyReference<S> getPropertyRef(T data){
        return fillProperty(data)? propertyRef : null;
    }

    private boolean fillProperty(T data) {
        if (getProperty() == null || getProperty().isEmpty() || data == null) return false;

        try {
            // we attempt to cache the property reference here, as otherwise
            // performance suffers when working in large data models. For
            // a bit of reference, refer to RT-13937.
            if (dataClass == null || previousProperty == null ||
                    ! dataClass.equals(data.getClass()) ||
                    ! previousProperty.equals(getProperty())) {

                // create a new PropertyReference
                this.dataClass = data.getClass();
                this.previousProperty = getProperty();
                this.propertyRef = new PropertyReference<>(data.getClass(), getProperty());
            }
        } catch (IllegalStateException e) {
            // log the warning and move on
            LOG.warn("Can not retrieve property '{}' in PropertyValueFactory: {} with provided class type: {}", getProperty(), this, data.getClass());
            LOG.warn("",e);
        }
        return true;
    }
}