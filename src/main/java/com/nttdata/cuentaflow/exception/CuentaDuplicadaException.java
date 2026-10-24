package com.nttdata.cuentaflow.exception;

public class CuentaDuplicadaException extends RuntimeException {

    public CuentaDuplicadaException(String numero) {
        super("El cliente ya tiene una cuenta de ese tipo y moneda: " + numero);
    }
}
