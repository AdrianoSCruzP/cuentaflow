package com.nttdata.cuentaflow.service;

import com.nttdata.cuentaflow.dto.CuentaRequest;
import com.nttdata.cuentaflow.model.Cuenta;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

/** La capa de servicio usa RxJava 3. */
public interface CuentaService {

    /** Todas las cuentas (puede ser un volumen grande): Flowable, con backpressure. */
    Flowable<Cuenta> listar();

    /** Cuentas de un cliente ordenadas por numero (0..N): Observable (RN-07). */
    Observable<Cuenta> listarPorCliente(String dni);

    /** Una cuenta por numero; si no existe, error CuentaNoEncontradaException (RN-06). */
    Single<Cuenta> consultar(String numero);

    /** Registra una cuenta nueva aplicando RN-02 a RN-05. */
    Single<Cuenta> registrar(CuentaRequest request);
}
