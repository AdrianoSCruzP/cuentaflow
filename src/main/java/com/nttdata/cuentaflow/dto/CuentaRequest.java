package com.nttdata.cuentaflow.dto;

import com.nttdata.cuentaflow.model.Moneda;
import com.nttdata.cuentaflow.model.TipoCuenta;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

/** Cuerpo del POST /api/v1/cuentas. Las anotaciones aplican la regla RN-01. */
public record CuentaRequest(
        @NotBlank
        @Pattern(regexp = "\\d{8}", message = "debe tener 8 digitos")
        String dni,
        @NotBlank
        String titular,
        @NotNull
        TipoCuenta tipo,
        @NotNull
        Moneda moneda,
        @NotNull
        @PositiveOrZero
        BigDecimal saldoInicial) {
}
