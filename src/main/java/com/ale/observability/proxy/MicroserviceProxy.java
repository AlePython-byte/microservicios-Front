package com.ale.observability.proxy;

public interface MicroserviceProxy<T> {

    T execute(String operation, Object... params);
}