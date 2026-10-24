package com.nttdata.cuentaflow.model;

/** Monedas soportadas con su codigo para el numero de cuenta (RN-02). */
public enum Moneda {

    PEN("01"),
    USD("02");

    private final String codigo;

    Moneda(String codigo) {
        this.codigo = codigo;
    }

    public String codigo() {
        return codigo;
    }
}
