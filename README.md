# Tlali-Back

Backend REST de Tlali para recibir lecturas de sensores desde un ESP32.

## Stack

- Java 17
- Spring Boot
- Maven
- Spring Web MVC
- Spring Data JPA
- PostgreSQL
- Validation

## Ejecutar

```powershell
.\mvnw.cmd spring-boot:run
```

La API queda disponible en `http://localhost:8080`.

## Endpoints iniciales

- `GET /api/v1/health`
- `POST /api/v1/sensor-readings`
- `GET /api/v1/sensor-readings/latest?limit=10`

Ejemplo de lectura:

```json
{
  "deviceId": "esp32-greenhouse-01",
  "greenhouseId": "greenhouse-main",
  "temperatureCelsius": 24.8,
  "humidityPercent": 67.5,
  "soilMoisturePercent": 41.2,
  "lightLux": 1180,
  "batteryVoltage": 3.7
}
```

## Pruebas

```powershell
.\mvnw.cmd test
```
