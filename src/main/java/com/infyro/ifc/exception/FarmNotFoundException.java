package com.infyro.ifc.exception;

public class FarmNotFoundException extends RuntimeException{
    public FarmNotFoundException(String message) {
        super(message);
    }
}
