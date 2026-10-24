package com.nttdata.cuentaflow.dto;

import java.time.LocalDateTime;

/** Formato comun de error (RN-08). */
public record ErrorRespuesta(String codigo, String mensaje, LocalDateTime timestamp) {

    public static ErrorRespuesta of(String codigo, String mensaje) {
        return new ErrorRespuesta(codigo, mensaje, LocalDateTime.now());
    }
}
