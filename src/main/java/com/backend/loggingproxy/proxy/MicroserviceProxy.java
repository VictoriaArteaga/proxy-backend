package com.backend.loggingproxy.proxy;

public interface MicroserviceProxy<T> {

    T execute(String operation, Object... params);
}
