package com.nttdata.cuentaflow.util;

import com.nttdata.cuentaflow.model.Moneda;
import com.nttdata.cuentaflow.model.TipoCuenta;

public final class GeneradorNumeroCuenta {

    private GeneradorNumeroCuenta() {
    }

    /**
     * RN-02: prefijo del tipo + "-" + dni + "-" + codigo de la moneda.
     * Ejemplo: AHORRO, 45678912, PEN  ->  191-45678912-01
     */
    public static String generar(TipoCuenta tipo, String dni, Moneda moneda) {
        return tipo.prefijo() + "-" + dni + "-" + moneda.codigo();
    }
}
