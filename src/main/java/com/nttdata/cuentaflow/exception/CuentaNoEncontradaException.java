package com.nttdata.cuentaflow.exception;

public class CuentaNoEncontradaException extends RuntimeException {

    public CuentaNoEncontradaException(String numero) {
        super("No existe la cuenta " + numero);
    }
}
