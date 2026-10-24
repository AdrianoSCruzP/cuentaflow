package com.nttdata.cuentaflow.config;

import com.nttdata.cuentaflow.model.Cuenta;
import com.nttdata.cuentaflow.model.Moneda;
import com.nttdata.cuentaflow.model.TipoCuenta;
import com.nttdata.cuentaflow.repository.CuentaRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Carga 3 cuentas de ejemplo si la coleccion esta vacia.
 * Es codigo de arranque: aqui si se usa subscribe() porque no hay una peticion HTTP que se suscriba.
 */
@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    private final CuentaRepository repository;

    public DataLoader(CuentaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        List<Cuenta> ejemplos = List.of(
                new Cuenta("191-45678912-01", "45678912", "Juan Lopez", TipoCuenta.AHORRO, Moneda.PEN,
                        new BigDecimal("1500.00"), Cuenta.ESTADO_ACTIVA, LocalDate.of(2026, 1, 15)),
                new Cuenta("193-45678912-02", "45678912", "Juan Lopez", TipoCuenta.CORRIENTE, Moneda.USD,
                        new BigDecimal("800.00"), Cuenta.ESTADO_ACTIVA, LocalDate.of(2026, 3, 2)),
                new Cuenta("191-78999912-01", "78999912", "Maria Sanchez", TipoCuenta.AHORRO, Moneda.PEN,
                        new BigDecimal("3200.00"), Cuenta.ESTADO_ACTIVA, LocalDate.of(2026, 5, 20)));

        repository.count()
                .filter(total -> total == 0)
                .flatMapMany(total -> repository.saveAll(ejemplos))
                .subscribe(
                        cuenta -> log.info("Cuenta de ejemplo cargada: {}", cuenta.numero()),
                        error -> log.warn("No se pudieron cargar las cuentas de ejemplo: {}", error.getMessage()));
    }
}
