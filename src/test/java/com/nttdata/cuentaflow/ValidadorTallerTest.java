package com.nttdata.cuentaflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.nttdata.cuentaflow.controller.CuentaController;
import com.nttdata.cuentaflow.dto.CuentaRequest;
import com.nttdata.cuentaflow.exception.CuentaDuplicadaException;
import com.nttdata.cuentaflow.exception.CuentaNoEncontradaException;
import com.nttdata.cuentaflow.exception.GlobalExceptionHandler;
import com.nttdata.cuentaflow.exception.ReglaNegocioException;
import com.nttdata.cuentaflow.model.Cuenta;
import com.nttdata.cuentaflow.model.Moneda;
import com.nttdata.cuentaflow.model.TipoCuenta;
import com.nttdata.cuentaflow.repository.CuentaRepository;
import com.nttdata.cuentaflow.service.CuentaService;
import com.nttdata.cuentaflow.service.impl.CuentaServiceImpl;
import com.nttdata.cuentaflow.util.GeneradorNumeroCuenta;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * VALIDADOR DEL TALLER - NO MODIFICAR.
 * 20 verificaciones (V01 a V20). Cada prueba en verde vale 1 punto.
 * No necesita MongoDB: el repositorio se simula con Mockito.
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
class ValidadorTallerTest {

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    private static final Cuenta C1 = new Cuenta("191-45678912-01", "45678912", "Juan Lopez", TipoCuenta.AHORRO,
            Moneda.PEN, new BigDecimal("1500.00"), "ACTIVA", LocalDate.of(2026, 1, 15));
    private static final Cuenta C2 = new Cuenta("193-45678912-02", "45678912", "Juan Lopez", TipoCuenta.CORRIENTE,
            Moneda.USD, new BigDecimal("800.00"), "ACTIVA", LocalDate.of(2026, 3, 2));
    private static final Cuenta C3 = new Cuenta("191-78999912-01", "78999912", "Maria Sanchez", TipoCuenta.AHORRO,
            Moneda.PEN, new BigDecimal("3200.00"), "ACTIVA", LocalDate.of(2026, 5, 20));

    private CuentaRepository repository;
    private CuentaService service;
    private WebTestClient client;

    @BeforeEach
    void setUp() {
        repository = mock(CuentaRepository.class);
        service = new CuentaServiceImpl(repository);
        client = WebTestClient.bindToController(new CuentaController(service))
                .controllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private static CuentaRequest req(String dni, String titular, TipoCuenta tipo, Moneda moneda, String saldo) {
        return new CuentaRequest(dni, titular, tipo, moneda, saldo == null ? null : new BigDecimal(saldo));
    }

    private static Set<String> camposConError(CuentaRequest request) {
        return VALIDATOR.validate(request).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .collect(Collectors.toSet());
    }

    private void simularRegistroOk() {
        when(repository.existsById(anyString())).thenReturn(Mono.just(false));
        when(repository.save(any(Cuenta.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
    }

    // ---------------- BLOQUE 1 · Modelo, DTO y numero de cuenta (TODO 1-3)

    @Test
    @DisplayName("V01 [B1] Numero de cuenta AHORRO en PEN: 191-<dni>-01 (RN-02)")
    void v01() {
        assertEquals("191-45678912-01", GeneradorNumeroCuenta.generar(TipoCuenta.AHORRO, "45678912", Moneda.PEN));
    }

    @Test
    @DisplayName("V02 [B1] Numero de cuenta CORRIENTE en USD: 193-<dni>-02 (RN-02)")
    void v02() {
        assertEquals("193-11111111-02", GeneradorNumeroCuenta.generar(TipoCuenta.CORRIENTE, "11111111", Moneda.USD));
    }

    @Test
    @DisplayName("V03 [B1] Cuenta.abrir: estado ACTIVA, fecha de hoy y saldo inicial (RN-05)")
    void v03() {
        Cuenta c = Cuenta.abrir("191-11111111-01", req("11111111", "Ana Torres", TipoCuenta.AHORRO, Moneda.PEN, "100.00"));
        assertEquals("191-11111111-01", c.numero());
        assertEquals("Ana Torres", c.titular());
        assertEquals("ACTIVA", c.estado());
        assertEquals(LocalDate.now(), c.fechaApertura());
        assertEquals(new BigDecimal("100.00"), c.saldo());
    }

    @Test
    @DisplayName("V04 [B1] Validacion: el DNI debe tener 8 digitos (RN-01)")
    void v04() {
        assertTrue(camposConError(req("123", "Ana Torres", TipoCuenta.AHORRO, Moneda.PEN, "100")).contains("dni"));
        assertTrue(camposConError(req("45678912", "Ana Torres", TipoCuenta.AHORRO, Moneda.PEN, "100")).isEmpty());
    }

    @Test
    @DisplayName("V05 [B1] Validacion: titular, tipo, moneda obligatorios y saldo no negativo (RN-01)")
    void v05() {
        Set<String> campos = camposConError(req("45678912", " ", null, null, "-10"));
        assertTrue(campos.containsAll(Set.of("titular", "tipo", "moneda", "saldoInicial")), "Campos con error: " + campos);
    }

    // ---------------- BLOQUE 2 · Servicio de consultas con RxJava (TODO 4-6)

    @Test
    @DisplayName("V06 [B2] listar(): Flowable con todas las cuentas")
    void v06() {
        when(repository.findAll()).thenReturn(Flux.just(C1, C2, C3));
        service.listar().test().assertValueCount(3).assertNoErrors().assertComplete();
    }

    @Test
    @DisplayName("V07 [B2] listarPorCliente(): cuentas del DNI ordenadas por numero (RN-07)")
    void v07() {
        when(repository.findByDni("45678912")).thenReturn(Flux.just(C2, C1));
        service.listarPorCliente("45678912").test().assertValues(C1, C2).assertComplete();
    }

    @Test
    @DisplayName("V08 [B2] listarPorCliente(): cliente sin cuentas termina vacio, sin error (RN-07)")
    void v08() {
        when(repository.findByDni("11111111")).thenReturn(Flux.empty());
        service.listarPorCliente("11111111").test().assertNoValues().assertNoErrors().assertComplete();
    }

    @Test
    @DisplayName("V09 [B2] consultar(): devuelve la cuenta existente")
    void v09() {
        when(repository.findById("191-45678912-01")).thenReturn(Mono.just(C1));
        service.consultar("191-45678912-01").test().assertValue(C1).assertComplete();
    }

    @Test
    @DisplayName("V10 [B2] consultar(): cuenta inexistente emite CuentaNoEncontradaException (RN-06)")
    void v10() {
        when(repository.findById("999")).thenReturn(Mono.empty());
        service.consultar("999").test().assertError(CuentaNoEncontradaException.class);
    }

    // ---------------- BLOQUE 3 · Servicio de registro con RxJava (TODO 7-8)

    @Test
    @DisplayName("V11 [B3] registrar(): guarda la cuenta con numero generado y estado ACTIVA")
    void v11() {
        simularRegistroOk();
        service.registrar(req("11111111", "Ana Torres", TipoCuenta.AHORRO, Moneda.PEN, "100.00"))
                .test()
                .assertValue(c -> c.numero().equals("191-11111111-01") && c.estado().equals("ACTIVA"))
                .assertComplete();
    }

    @Test
    @DisplayName("V12 [B3] registrar(): cuenta del mismo tipo y moneda emite CuentaDuplicadaException y no guarda (RN-03)")
    void v12() {
        when(repository.existsById("191-45678912-01")).thenReturn(Mono.just(true));
        service.registrar(req("45678912", "Juan Lopez", TipoCuenta.AHORRO, Moneda.PEN, "50.00"))
                .test()
                .assertError(CuentaDuplicadaException.class);
        verify(repository, never()).save(any(Cuenta.class));
    }

    @Test
    @DisplayName("V13 [B3] registrar(): CORRIENTE exige saldo >= 500; si no, error sin consultar MongoDB (RN-04)")
    void v13() {
        service.registrar(req("11111111", "Ana Torres", TipoCuenta.CORRIENTE, Moneda.PEN, "200.00"))
                .test()
                .assertError(e -> e instanceof ReglaNegocioException r && "SALDO_MINIMO".equals(r.getCodigo()));
        verifyNoInteractions(repository);

        simularRegistroOk();
        service.registrar(req("11111111", "Ana Torres", TipoCuenta.CORRIENTE, Moneda.PEN, "500.00"))
                .test()
                .assertValue(c -> c.numero().equals("193-11111111-01"));
    }

    // ---------------- BLOQUE 4 · Controller WebFlux (TODO 9-11)

    @Test
    @DisplayName("V14 [B4] GET /api/v1/cuentas -> 200 con 3 cuentas")
    void v14() {
        when(repository.findAll()).thenReturn(Flux.just(C1, C2, C3));
        client.get().uri("/api/v1/cuentas").exchange()
                .expectStatus().isOk()
                .expectBodyList(Cuenta.class).hasSize(3);
    }

    @Test
    @DisplayName("V15 [B4] GET /api/v1/clientes/{dni}/cuentas -> 200 con 2 cuentas ordenadas")
    void v15() {
        when(repository.findByDni("45678912")).thenReturn(Flux.just(C2, C1));
        client.get().uri("/api/v1/clientes/45678912/cuentas").exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].numero").isEqualTo("191-45678912-01");
    }

    @Test
    @DisplayName("V16 [B4] GET /api/v1/cuentas/{numero} -> 200; inexistente -> 404 CUENTA_NO_ENCONTRADA")
    void v16() {
        when(repository.findById("191-45678912-01")).thenReturn(Mono.just(C1));
        when(repository.findById("999")).thenReturn(Mono.empty());
        client.get().uri("/api/v1/cuentas/191-45678912-01").exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.titular").isEqualTo("Juan Lopez");
        client.get().uri("/api/v1/cuentas/999").exchange()
                .expectStatus().isNotFound()
                .expectBody().jsonPath("$.codigo").isEqualTo("CUENTA_NO_ENCONTRADA");
    }

    @Test
    @DisplayName("V17 [B4] POST /api/v1/cuentas -> 201 Created con el numero generado")
    void v17() {
        simularRegistroOk();
        client.post().uri("/api/v1/cuentas").contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req("11111111", "Ana Torres", TipoCuenta.AHORRO, Moneda.PEN, "100.00"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody().jsonPath("$.numero").isEqualTo("191-11111111-01");
    }

    // ---------------- BLOQUE 5 · Manejo de errores (TODO 12-14)

    @Test
    @DisplayName("V18 [B5] POST duplicado -> 409 CUENTA_DUPLICADA")
    void v18() {
        when(repository.existsById("191-45678912-01")).thenReturn(Mono.just(true));
        client.post().uri("/api/v1/cuentas").contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req("45678912", "Juan Lopez", TipoCuenta.AHORRO, Moneda.PEN, "50.00"))
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody().jsonPath("$.codigo").isEqualTo("CUENTA_DUPLICADA");
    }

    @Test
    @DisplayName("V19 [B5] POST CORRIENTE con saldo 200 -> 400 SALDO_MINIMO")
    void v19() {
        client.post().uri("/api/v1/cuentas").contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req("11111111", "Ana Torres", TipoCuenta.CORRIENTE, Moneda.PEN, "200.00"))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody().jsonPath("$.codigo").isEqualTo("SALDO_MINIMO");
    }

    @Test
    @DisplayName("V20 [B5] POST con DNI invalido -> 400 VALIDACION")
    void v20() {
        client.post().uri("/api/v1/cuentas").contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req("123", "Ana Torres", TipoCuenta.AHORRO, Moneda.PEN, "100.00"))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody().jsonPath("$.codigo").isEqualTo("VALIDACION");
    }
}
