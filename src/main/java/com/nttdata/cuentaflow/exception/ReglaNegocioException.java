package com.nttdata.cuentaflow.exception;

/** Error de regla de negocio con un codigo para el cliente (por ejemplo SALDO_MINIMO). */
public class ReglaNegocioException extends RuntimeException {

    private final String codigo;

    public ReglaNegocioException(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
