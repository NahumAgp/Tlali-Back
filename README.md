# Tlali Tlapixqui Back

Backend REST de Tlali Tlapixqui para recibir lecturas de sensores desde un ESP32.

## Stack

- Java 17
- Spring Boot
- Maven
- Spring Web MVC
- Spring Data JPA
- PostgreSQL
- Validation

## Ejecutar

El backend necesita la base local de Supabase escuchando en `localhost:54322`.

Desde el repo general puedes levantar Supabase local con:

```powershell
cd ..
npm install
npx supabase start
```

Luego inicia Spring Boot:

```powershell
.\mvnw.cmd spring-boot:run
```

La API queda disponible en `http://localhost:8080`.

## Endpoints iniciales

- `GET /api/v1/health`
- `POST /api/v1/auth/login`
- `GET /api/v1/auth/me`
- `POST /api/v1/sensor-readings`
- `GET /api/v1/sensor-readings/latest?limit=10`

## Seguridad

La API usa Spring Security con JWT.

Usuario inicial:

```text
Correo: superadmin@tlali.local
Password: SuperAdmin123!
```

Puedes cambiarlo con variables de entorno:

```powershell
TLALI_SUPERADMIN_EMAIL=admin@example.com
TLALI_SUPERADMIN_PASSWORD=change-me
TLALI_SUPERADMIN_NAME=Super Admin
TLALI_JWT_SECRET=change-this-secret-in-production-at-least-32-chars
TLALI_DEVICE_API_KEY=tlali-local-device-key
```

Login local:

```http
POST /api/v1/auth/login
```

```json
{
  "email": "superadmin@tlali.local",
  "password": "SuperAdmin123!"
}
```

El token se envia como:

```text
Authorization: Bearer <token>
```

Los dispositivos ESP32 pueden enviar lecturas sin login usando:

```text
X-Tlali-Device-Key: tlali-local-device-key
```

## Google OAuth

Configura estas variables para habilitar `GET /oauth2/authorization/google`:

```powershell
SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT_ID=your-client-id
SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT_SECRET=your-client-secret
SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_SCOPE=openid,email,profile
TLALI_FRONTEND_AUTH_CALLBACK=http://localhost:5173/auth/callback
```

En Google Cloud usa este redirect URI:

```text
http://localhost:8080/login/oauth2/code/google
```

Ejemplo de lectura:

```json
{
  "deviceId": "esp32-tlali-sensor-01",
  "siteId": "tlali-tlapixqui-main",
  "temperatureCelsius": 24.8,
  "humidityPercent": 67.5,
  "soilMoisturePercent": 41.2,
  "lightLux": 1180,
  "batteryVoltage": 3.7
}
```

Ejemplo simulando un ESP32:

```powershell
Invoke-RestMethod `
  -Uri http://localhost:8080/api/v1/sensor-readings `
  -Method POST `
  -Headers @{ "X-Tlali-Device-Key" = "tlali-local-device-key" } `
  -ContentType "application/json" `
  -Body '{
    "deviceId": "esp32-tlali-sensor-01",
    "siteId": "tlali-tlapixqui-main",
    "temperatureCelsius": 25.4,
    "humidityPercent": 61.2,
    "soilMoisturePercent": 42.8,
    "lightLux": 1800,
    "batteryVoltage": 3.74
  }'
```

## Pruebas

```powershell
.\mvnw.cmd test
```
