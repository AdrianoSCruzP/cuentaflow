package com.nttdata.cuentaflow.model;

import java.math.BigDecimal;

/** Tipos de cuenta con su prefijo de numeracion (RN-02) y saldo minimo de apertura (RN-04). */
public enum TipoCuenta {

    AHORRO("191", BigDecimal.ZERO),
    CORRIENTE("193", new BigDecimal("500.00"));

    private final String prefijo;
    private final BigDecimal saldoMinimo;

    TipoCuenta(String prefijo, BigDecimal saldoMinimo) {
        this.prefijo = prefijo;
        this.saldoMinimo = saldoMinimo;
    }

    public String prefijo() {
        return prefijo;
    }

    public BigDecimal saldoMinimo() {
        return saldoMinimo;
    }
}
