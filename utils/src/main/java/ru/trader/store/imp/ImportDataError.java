package ru.trader.store.imp;

public class ImportDataError extends Error {
    public ImportDataError() {
        super();
    }

    public ImportDataError(String message) {
        super(message);
    }

    public ImportDataError(String message, Throwable cause) {
        super(message, cause);
    }

    public ImportDataError(Throwable cause) {
        super(cause);
    }
}
