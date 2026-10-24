# CuentaFlow

API REST para consultar y registrar cuentas bancarias, desarrollada como ejercicio de programación reactiva con **Java 17, Spring Boot 4.1.2, Spring WebFlux, RxJava 3 y MongoDB**.

## Qué hace

- Lista todas las cuentas registradas.
- Consulta las cuentas de un cliente por DNI y las ordena por número de cuenta.
- Busca una cuenta por su número.
- Registra cuentas de ahorro o corrientes en soles (`PEN`) o dólares (`USD`).
- Valida los datos de entrada y el saldo mínimo de apertura, y comprueba si la cuenta ya existe antes de guardarla.
- Devuelve errores con un código, un mensaje y una fecha y hora.

El controlador utiliza `Mono` y `Flux` de Reactor; el servicio utiliza tipos de RxJava como `Single`, `Observable`, `Flowable`, `Maybe` y `Completable`. El repositorio accede a MongoDB mediante su driver reactivo.

## Requisitos

- **JDK 17** y la variable `JAVA_HOME` apuntando al JDK.
- **Maven 3.9 o superior**, disponible en la terminal.
- **MongoDB** para ejecutar la API. El archivo Docker Compose incluido utiliza MongoDB 7.
- **Docker con Docker Compose**, solo si eliges levantar MongoDB con Docker.

Comprueba Java y Maven:

```bash
java -version
mvn -version
```

Ejecuta los siguientes comandos desde la carpeta que contiene `pom.xml`. Maven necesita acceso a internet para descargar las dependencias que aún no estén en su caché local.

## Compilar y probar

Para compilar, ejecutar las pruebas y generar el archivo JAR ejecutable:

```bash
mvn clean package
```

Si el proceso termina correctamente, el JAR se genera en:

```text
target/cuentaflow-0.0.1-SNAPSHOT.jar
```

También puedes ejecutar cada paso por separado:

| Comando | Qué hace |
| --- | --- |
| `mvn clean compile` | Limpia los resultados anteriores y compila el código principal. |
| `mvn test` | Ejecuta las 20 pruebas de `ValidadorTallerTest`. |

**No necesitas MongoDB para compilar ni para ejecutar estas pruebas:** el repositorio se simula con Mockito. Las pruebas verifican el modelo, las validaciones, el servicio, los endpoints y las respuestas de error.

## Ejecutar la API

### 1. Iniciar MongoDB

Con Docker, ejecuta:

```bash
docker compose up -d
```

La conexión predeterminada es `mongodb://localhost:27017` y la base de datos se llama `cuentaflowdb`. También puedes usar una instalación local de MongoDB en esa dirección.

Para usar otra conexión, como MongoDB Atlas, define `MONGODB_URI` antes de iniciar la aplicación. En PowerShell:

```powershell
$env:MONGODB_URI = "<tu_uri_de_mongodb>"
```

La configuración está en `src/main/resources/application.yml`.

### 2. Iniciar la aplicación

Con Maven:

```bash
mvn spring-boot:run
```

O, si ya generaste el JAR con `mvn clean package`:

```bash
java -jar target/cuentaflow-0.0.1-SNAPSHOT.jar
```

La API queda disponible en **http://localhost:8085/api/v1**. Al arrancar, carga tres cuentas de ejemplo si la colección está vacía.

En IntelliJ IDEA también puedes abrir el proyecto como Maven, seleccionar el JDK 17 y ejecutar la clase `CuentaflowApplication`.

## Endpoints

| Método | Ruta | Descripción | Respuesta correcta |
| --- | --- | --- | --- |
| GET | `/api/v1/cuentas` | Lista todas las cuentas. | `200 OK` |
| GET | `/api/v1/clientes/{dni}/cuentas` | Lista las cuentas del cliente, ordenadas por número. | `200 OK` |
| GET | `/api/v1/cuentas/{numero}` | Consulta una cuenta por su número. | `200 OK` |
| POST | `/api/v1/cuentas` | Registra una cuenta nueva. | `201 Created` |

Si el cliente no tiene cuentas, la consulta por DNI devuelve una lista vacía (`[]`) con estado `200`.

### Ejemplo de registro

Envía un `POST` a `http://localhost:8085/api/v1/cuentas`, con el encabezado `Content-Type: application/json` y este cuerpo:

```json
{
  "dni": "11111111",
  "titular": "Ana Torres",
  "tipo": "AHORRO",
  "moneda": "PEN",
  "saldoInicial": 100.00
}
```

La cuenta se crea con el número `191-11111111-01`, estado `ACTIVA` y la fecha de apertura del día del registro.

Para probar las peticiones, importa `CuentaFlow.postman_collection.json` en Postman. La variable `baseUrl` ya está configurada como `http://localhost:8085`.

## Validaciones y errores

- El DNI debe contener exactamente ocho dígitos.
- El titular es obligatorio y no puede estar vacío ni contener solo espacios.
- El tipo de cuenta, la moneda y el saldo inicial son obligatorios.
- El saldo inicial no puede ser negativo. Las cuentas `CORRIENTE` requieren al menos `500.00`, en la moneda elegida.
- El número se forma con el prefijo del tipo (`191` para `AHORRO`, `193` para `CORRIENTE`), el DNI y el código de moneda (`01` para `PEN`, `02` para `USD`).
- Si ya existe una cuenta para el mismo DNI, tipo y moneda, el registro devuelve un error de cuenta duplicada.

| HTTP | Código | Motivo |
| --- | --- | --- |
| `400` | `VALIDACION` | Algún campo incumple las validaciones del formulario de registro. |
| `400` | `SALDO_MINIMO` | El saldo inicial es menor al mínimo del tipo de cuenta. |
| `404` | `CUENTA_NO_ENCONTRADA` | No existe la cuenta consultada por número. |
| `409` | `CUENTA_DUPLICADA` | Ya existe la cuenta que se intenta registrar. |

Estas respuestas utilizan el siguiente formato:

```json
{
  "codigo": "VALIDACION",
  "mensaje": "dni: debe tener 8 digitos",
  "timestamp": "2026-10-24T12:00:00"
}
```

## Organización del código

| Paquete | Responsabilidad |
| --- | --- |
| `controller` | Expone los endpoints HTTP. |
| `service` | Aplica las reglas de negocio y coordina las operaciones reactivas. |
| `repository` | Consulta y guarda las cuentas en MongoDB. |
| `model` | Define la cuenta, los tipos de cuenta y las monedas. |
| `dto` | Define los datos de registro y el formato de los errores. |
| `exception` | Traduce los errores de negocio y validación a respuestas HTTP. |
| `util` | Genera los números de cuenta. |
| `config` | Carga los datos de ejemplo al arrancar. |
