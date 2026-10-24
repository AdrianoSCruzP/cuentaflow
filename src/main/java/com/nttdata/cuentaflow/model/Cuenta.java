package com.nttdata.cuentaflow.model;

import com.nttdata.cuentaflow.dto.CuentaRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Documento de MongoDB (coleccion "cuentas").
 * El numero de cuenta es el _id: asi la busqueda por numero es findById.
 */
@Document(collection = "cuentas")
public record Cuenta(
        @Id String numero,
        String dni,
        String titular,
        TipoCuenta tipo,
        Moneda moneda,
        BigDecimal saldo,
        String estado,
        LocalDate fechaApertura) {

    public static final String ESTADO_ACTIVA = "ACTIVA";

    /** RN-05: crea una cuenta nueva a partir de la solicitud y el numero generado. */
    public static Cuenta abrir(String numero, CuentaRequest request) {
        return new Cuenta(
                numero, request.dni(),
                request.titular(),
                request.tipo(),
                request.moneda(),
                request.saldoInicial(),
                ESTADO_ACTIVA,
                LocalDate.now());
    }
}
