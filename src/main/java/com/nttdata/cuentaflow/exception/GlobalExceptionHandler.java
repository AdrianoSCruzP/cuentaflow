package com.nttdata.cuentaflow.exception;

import com.nttdata.cuentaflow.dto.ErrorRespuesta;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

/** Traduce las excepciones a respuestas HTTP con ErrorRespuesta (RN-08). */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Ejemplo resuelto: cuenta inexistente -> 404 CUENTA_NO_ENCONTRADA (RN-06). */
    @ExceptionHandler(CuentaNoEncontradaException.class)
    public ResponseEntity<ErrorRespuesta> cuentaNoEncontrada(CuentaNoEncontradaException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorRespuesta.of("CUENTA_NO_ENCONTRADA", ex.getMessage()));
    }

    @ExceptionHandler(CuentaDuplicadaException.class)
    public ResponseEntity<ErrorRespuesta> cuentaDuplicada(CuentaDuplicadaException ex){
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorRespuesta.of("CUENTA_DUPLICADA", ex.getMessage()));
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorRespuesta> reglaNegocio (ReglaNegocioException ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorRespuesta.of("SALDO_MINIMO", ex.getMessage()));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ErrorRespuesta> validacion (WebExchangeBindException ex){
        String mensaje = ex.getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorRespuesta.of("VALIDACION", mensaje));
    }

}
