package com.nttdata.cuentaflow.repository;

import com.nttdata.cuentaflow.model.Cuenta;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

/**
 * Repositorio reactivo: todos sus metodos devuelven tipos de Reactor (Mono / Flux).
 * Metodos heredados que usaremos: findAll(), findById(id), existsById(id), save(entidad).
 */
@Repository
public interface CuentaRepository extends ReactiveMongoRepository<Cuenta, String> {

    /** Consulta derivada: Spring Data genera la query { "dni": ?0 } a partir del nombre. */
    Flux<Cuenta> findByDni(String dni);
}
