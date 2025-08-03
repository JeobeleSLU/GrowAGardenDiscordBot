package org.Utiilities;

import java.util.concurrent.Callable;

public interface JsonParser<T> extends Callable<T> {
    void setMessage(String message);
}
