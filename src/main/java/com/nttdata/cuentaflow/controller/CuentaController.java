package com.nttdata.cuentaflow.controller;

import com.nttdata.cuentaflow.dto.CuentaRequest;
import com.nttdata.cuentaflow.model.Cuenta;
import com.nttdata.cuentaflow.service.CuentaService;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * La capa web usa Spring WebFlux (Reactor): convierte los tipos RxJava del servicio a Mono / Flux.
 * Regla: el controller nunca llama a subscribe() ni a block(); WebFlux se suscribe por nosotros.
 */
@RestController
@RequestMapping("/api/v1")
public class CuentaController {

    private final CuentaService service;

    public CuentaController(CuentaService service) {
        this.service = service;
    }

    /** Ejemplo resuelto: GET /api/v1/cuentas  ->  Flowable (RxJava) a Flux (Reactor). */
    @GetMapping("/cuentas")
    public Flux<Cuenta> listar() {
        return Flux.from(service.listar());
    }

    @GetMapping ("clientes/{dni}/cuentas")
    public Flux<Cuenta> listarCuentasDni (@PathVariable String dni){
        return Flux.from(service.listarPorCliente(dni).toFlowable(BackpressureStrategy.BUFFER));
    }

    @GetMapping("cuentas/{numero}")
    public Mono<Cuenta> buscarCuenta (@PathVariable String numero){
        return Mono.from(service.consultar(numero).toFlowable());
    }

    @PostMapping("cuentas")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Cuenta> crearCuenta(@Valid @RequestBody CuentaRequest request) {
        return Mono.from(service.registrar(request).toFlowable());
    }
}
