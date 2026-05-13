# JKU Moebelrueckgabe - IoT Prototyp

Spring Boot Backend + React Frontend zur Integration von NFC-Scans in den JKU Moebelprozess.

Mitarbeiter scannen NFC-Tags an Moebelstuecken mit dem Smartphone. Der Scan erfasst automatisch den GPS-Standort und aktualisiert das Moebelstueck in der Datenbank. Eine Kartenansicht zeigt alle Moebelstuecke in Echtzeit auf einer OpenStreetMap-Karte.

## Voraussetzungen

- Docker Desktop
- Node.js 18+
- Java 17+ (nur bei Option A, nicht noetig bei Option B)
- ngrok (optional, nur fuer Handy-Tests mit GPS - reicht wenn eine Person im Team den Account hat)

## Inbetriebnahme

### Option A: Entwicklungsmodus (Backend lokal)

Zum Entwickeln und Debuggen. Backend laeuft direkt, nur die DB im Docker.

```bash
# 1. Datenbank starten
docker compose up -d db

# 2. Backend starten (Mac/Linux)
./mvnw spring-boot:run

# 2. Backend starten (Windows)
mvnw.cmd spring-boot:run

# 3. Frontend starten (neues Terminal)
cd frontend
npm install
npm run dev
```

Beim ersten Start werden alle Maven-Dependencies heruntergeladen, das kann einige Minuten dauern.

### Option B: Backend + DB in Docker

Backend und Datenbank laufen komplett im Docker. Kein Java noetig.

```bash
# 1. Backend + DB starten
docker compose up -d

# 2. Frontend starten (neues Terminal)
cd frontend
npm install
npm run dev
```

### Im Browser oeffnen

`http://localhost:5173` - Die Karte mit allen Moebelstuecken wird angezeigt.

Die Datenbank wird beim ersten Start automatisch mit 5 Test-Moebelstuecken befuellt.

### Stoppen

```bash
# Alles stoppen
docker compose down

# Nur Backend/DB stoppen, Daten bleiben erhalten
docker compose stop
```

## Handy-Tests mit NFC und GPS

GPS funktioniert nur ueber HTTPS. Dafuer wird ngrok verwendet:

```bash
ngrok http 5173 --domain=erasable-petted-turkey.ngrok-free.dev
```

Die NFC-Tags oeffnen URLs im Format:
```
https://erasable-petted-turkey.ngrok-free.dev/scan?id=MOEBL-0001
```

### NFC-Tags

| Tag-ID     | Bezeichnung            | Typ            | NFC-URL |
|------------|------------------------|----------------|---------|
| MOEBL-0001 | Schreibtisch Modell A  | Schreibtisch   | https://erasable-petted-turkey.ngrok-free.dev/scan?id=MOEBL-0001 |
| MOEBL-0002 | Drehstuhl Standard     | Stuhl          | https://erasable-petted-turkey.ngrok-free.dev/scan?id=MOEBL-0002 |
| MOEBL-0003 | Rollcontainer 3-Laden  | Rollcontainer  | https://erasable-petted-turkey.ngrok-free.dev/scan?id=MOEBL-0003 |
| MOEBL-0004 | Besprechungstisch      | Tisch          | https://erasable-petted-turkey.ngrok-free.dev/scan?id=MOEBL-0004 |
| MOEBL-0005 | Aktenschrank 2-tuerig  | Schrank        | https://erasable-petted-turkey.ngrok-free.dev/scan?id=MOEBL-0005 |

## Camunda 8 Integration

Camunda ist optional und wird ueber ein Spring-Profil aktiviert. Ohne Camunda funktioniert der Prototyp vollstaendig (Scans, Karte, Zustandsuebergaenge).

Voraussetzung: Camunda 8 Docker laeuft lokal (Zeebe auf Port 26500, Operate auf Port 8080).

```bash
# Mac/Linux
./mvnw spring-boot:run -Dspring-boot.run.profiles=camunda

# Windows
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=camunda
```

Bei einem Scan mit Event-Typ RUECKGABE wird ein Camunda-Prozess gestartet. Alle anderen Event-Typen senden eine Message an den laufenden Prozess.

## API-Endpoints

| Methode | Endpoint                     | Beschreibung                    |
|---------|------------------------------|---------------------------------|
| GET     | /api/moebelstuck             | Alle Moebelstuecke auflisten    |
| GET     | /api/moebelstuck/{id}        | Detail mit Scan-History         |
| POST    | /api/moebelstuck             | Neues Moebelstueck anlegen      |
| POST    | /api/moebelstuck/reset       | Alle auf Standardwerte zuruecksetzen |
| POST    | /api/moebelstuck/{id}/reset  | Einzelnes zuruecksetzen         |
| POST    | /api/scan                    | NFC-Scan verarbeiten            |
| GET     | /api/scan/{nfcTagId}         | Moebelstueck per Tag-ID suchen  |
| GET     | /swagger-ui/index.html       | Swagger API-Dokumentation       |

### Scan-Request Beispiel

```json
{
  "nfcTagId": "MOEBL-0001",
  "standortName": "Lager A",
  "standortLat": 48.3363,
  "standortLng": 14.3194,
  "eventTyp": "RUECKGABE",
  "gescanntVon": "Max Mustermann"
}
```

## Event-Typen und Zustandsuebergaenge

| Event-Typ      | BPMN-Schritt                          | Zustandsaenderung       |
|----------------|---------------------------------------|-------------------------|
| RUECKGABE      | Moebelstueck zur Rueckgabe anmelden   | -                       |
| PRUEFUNG       | Zustandspruefung durchfuehren         | -                       |
| EINLAGERUNG    | Moebelstueck einlagern                | -                       |
| TRANSPORT      | Transport zum Lager                   | -                       |
| REPARATUR      | Reparatur starten                     | -> IN_REPARATUR         |
| ENTSORGUNG     | Moebelstueck entsorgen                | -> ENTSORGT             |
| TEILDEMONTAGE  | Teilweise beschaedigt, Teile entnehmen| -> TEILWEISE_BESCHAEDIGT|

## Projektstruktur

```
iot-moebelrueckgabe/
  Dockerfile                      # Backend als Docker-Image
  docker-compose.yml              # PostgreSQL + Backend im Docker
  pom.xml                         # Maven-Konfiguration
  mvnw / mvnw.cmd                 # Maven Wrapper (Mac/Linux / Windows)
  src/main/java/jku/
    IntegrationIotApplication.java  # Spring Boot Einstiegspunkt
    BpmnDeployer.java               # Automatisches BPMN-Deployment
    DataSeeder.java                 # Test-Moebelstuecke beim Start anlegen
    WebConfig.java                  # CORS-Konfiguration
    api/                            # Request/Response-Klassen
    controller/                     # REST-Controller
    entity/                         # JPA-Entities (Moebelstuck, ScanHistory, ...)
    repository/                     # Spring Data Repositories
    service/                        # Scan-Logik und Camunda-Integration
  src/main/resources/
    application.yml                 # Konfiguration (DB, Server)
    application-camunda.yml         # Camunda-Profil
    moebelprozess.bpmn              # BPMN-Prozessmodell
  frontend/
    src/
      pages/MapPage.jsx             # Kartenansicht mit Leaflet/OpenStreetMap
      pages/ScanPage.jsx            # NFC-Scan-Seite mit GPS
      pages/AdminPage.jsx           # Verwaltung und Reset
      components/MoebelPopup.jsx    # Popup fuer Kartenmarker
      components/ScanHistory.jsx    # Scan-Verlauf als Timeline
    vite.config.js                  # Vite Dev-Server mit Proxy auf Backend

```

## Technologien

- **Backend:** Java 17, Spring Boot 4, Spring Data JPA, Camunda 8 SDK
- **Frontend:** React, Vite, Leaflet.js, OpenStreetMap
- **Datenbank:** PostgreSQL 16 (Docker)
- **Prozessengine:** Camunda 8 / Zeebe (optional)
- **NFC:** NTAG215 Tags mit URL-Encoding
- **HTTPS:** ngrok mit statischer Domain
