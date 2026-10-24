package com.nttdata.cuentaflow.service.impl;

import com.nttdata.cuentaflow.dto.CuentaRequest;
import com.nttdata.cuentaflow.exception.CuentaDuplicadaException;
import com.nttdata.cuentaflow.exception.CuentaNoEncontradaException;
import com.nttdata.cuentaflow.exception.ReglaNegocioException;
import com.nttdata.cuentaflow.model.Cuenta;
import com.nttdata.cuentaflow.repository.CuentaRepository;
import com.nttdata.cuentaflow.service.CuentaService;
import com.nttdata.cuentaflow.util.GeneradorNumeroCuenta;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.concurrent.Flow;

import io.reactivex.rxjava3.internal.operators.single.SingleError;
import org.springframework.stereotype.Service;

/**
 * Integracion: el repositorio devuelve tipos de Reactor (Mono / Flux) y aqui se convierten
 * a tipos de RxJava con fromPublisher (ambas librerias implementan Reactive Streams).
 */
@Service
public class CuentaServiceImpl implements CuentaService {

    private final CuentaRepository repository;

    public CuentaServiceImpl(CuentaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Flowable<Cuenta> listar() {
        return Flowable.fromPublisher(repository.findAll());
    }

    @Override
    public Observable<Cuenta> listarPorCliente(String dni) {
        return Observable.fromPublisher(repository.findByDni(dni)).sorted(Comparator.comparing(Cuenta::numero));
    }

    @Override
    public Single<Cuenta> consultar(String numero) {
        return Maybe.fromPublisher(repository.findById(numero))
                .switchIfEmpty(
                        Single.error(new CuentaNoEncontradaException(numero))
                );
    }

    @Override
    public Single<Cuenta> registrar(CuentaRequest request) {
        String numeroCuenta = GeneradorNumeroCuenta.generar(
                request.tipo(), request.dni(), request.moneda()
        );
        return validarSaldoMinimo(request)
                .andThen(
                        Single.defer(() ->
                                Single.fromPublisher(repository.existsById(numeroCuenta))
                        )
                )
                .flatMap(existe ->{
                    if (existe){
                        return Single.error(
                                new CuentaDuplicadaException(numeroCuenta)
                        );
                    }
                    return Single.fromPublisher(
                            repository.save(Cuenta.abrir(numeroCuenta, request))
                    );
                });
    }

    /** RN-04: el saldo inicial no puede ser menor al saldo minimo del tipo de cuenta. */
    Completable validarSaldoMinimo(CuentaRequest request) {
        return request.saldoInicial().compareTo(request.tipo().saldoMinimo()) < 0
                ? Completable.error(new ReglaNegocioException(
                "SALDO_MINIMO",
                "El saldo inicial no puede ser menor al saldo mínimo del tipo de cuenta"
        ))
                : Completable.complete();
    }
}
