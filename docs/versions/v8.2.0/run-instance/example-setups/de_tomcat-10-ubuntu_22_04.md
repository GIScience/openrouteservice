# Einrichtung von ORS v8 mit Tomcat 10 auf Ubuntu 22.04

Dieses Tutorial zeigt dir, wie du openrouteservice v8 mit Java 17 und Tomcat 10 einrichtest.

::: info
Zur [englischen Version](en_tomcat-10-ubuntu_22_04) dieses Tutorials.
:::

## Voraussetzungen

- Ubuntu 22.04 mit aktiviertem Systemd (Systemd ist standardmäßig aktiviert)
- Tomcat 10
- Java 17 oder höher

## Annahmen

- Es wird davon ausgegangen, dass nur eine Tomcat-10-Instanz auf deinem System läuft.
- Wenn du mehrere Tomcat-Dienste betreiben möchtest, bist du ein fortgeschrittener Benutzer und solltest die Anweisungen
  entsprechend anpassen.

## Vorbereitung der Tomcat-10-Umgebung

Die folgenden Schritte helfen dir, die Umgebung für Tomcat 10 vorzubereiten.

### Java 17 installieren

Openrouteservice v8 benötigt Java 17 oder höher.
Der Grund dafür ist die Einführung von Tomcat 10.
Du kannst auch eine neuere Version von Java verwenden, falls verfügbar.

```shell
# Aktualisiere den Paketindex
> sudo apt-get update
# Installiere Java 17 oder höher, curl und nano
> sudo apt-get install openjdk-17-jre-headless curl nano
```

- `openjdk-17-jre-headless` ist das Java-17-Laufzeitpaket ohne grafische Oberfläche.
  Für openrouteservice wird keine grafische Benutzeroberfläche benötigt.
- `curl` ist ein Befehlszeilentool zum Herunterladen von Dateien aus dem Web und wird verwendet, um bestimmte Ressourcen
  herunterzuladen.
- `nano` ist ein einfacher Texteditor, der zum Bearbeiten von Dateien verwendet wird.

Liste die verfügbaren Java-Versionen auf.

```shell
# Zeige verfügbare Optionen an und kopiere den Pfad zur Java-17-Installation
sudo update-alternatives --list java
```

Die Ausgabe sollte in etwa so aussehen:

```shell
[...]
/usr/lib/jvm/java-17-openjdk-amd64/
[...]
```

Aktualisiere die Standard-Java-Version auf Java 17 oder die installierte Version.

```shell
# Setze die Standard-Java-Version auf Java 17
> sudo update-alternatives --set java /usr/lib/jvm/java-17-openjdk-amd64/bin/java
```

::: info
**Hinweis:** Der Pfad zur Java-17-Installation kann je nach System unterschiedlich sein. \
**Hinweis:** Ab jetzt wird der Pfad zur Java-17-Installation als `JAVA_HOME` bezeichnet.
:::

### Einen neuen Benutzer für Tomcat 10 erstellen

Der Tomcat-10-Dienst sollte aus Sicherheitsgründen unter einem separaten Benutzer laufen.
Dieser Benutzer sollte keine Anmeldeberechtigungen haben und keine Befehle ausführen können.
Wir nennen diesen Benutzer `tomcat`.

```shell
# Erstelle einen neuen Benutzer für Tomcat 10
> useradd -r -m -U -d /opt/tomcat -s /bin/false tomcat
```

- `-r` erstellt einen Systembenutzer.
- `-m` erstellt ein Home-Verzeichnis `(/opt/tomcat)` für den Benutzer und setzt die notwendigen Berechtigungen.
- `-U` erstellt eine Gruppe mit demselben Namen wie der Benutzer.

### Tomcat 10 herunterladen und einrichten

```shell
# Setze die Tomcat-Version
> export TOMCAT_VERSION=10.1.33
# Lade das Tomcat-10-Tarball herunter
> curl -L https://dlcdn.apache.org/tomcat/tomcat-10/v$TOMCAT_VERSION/bin/apache-tomcat-$TOMCAT_VERSION.tar.gz > apache-tomcat-$TOMCAT_VERSION.tar.gz
# Entpacke die heruntergeladene Datei
> tar -xf apache-tomcat-$TOMCAT_VERSION.tar.gz
# Kopiere den Inhalt des entpackten Verzeichnisses in das Verzeichnis /opt/tomcat
> cp -R apache-tomcat-$TOMCAT_VERSION/** /opt/tomcat
# Bereinige das entpackte Verzeichnis
> rm -r apache-tomcat-$TOMCAT_VERSION apache-tomcat-$TOMCAT_VERSION.tar.gz
```

### Setze die notwendigen Berechtigungen für den Tomcat-10-Benutzer

Die folgenden Befehle setzen die notwendigen Berechtigungen für den tomcat-Benutzer.
Sie müssen als Root-Benutzer ausgeführt werden und sollten immer dann ausgeführt werden,
wenn Änderungen am Tomcat-Verzeichnis vorgenommen wurden.

```shell
> sudo chown -R tomcat:tomcat /opt/tomcat
> sudo chmod -R 754 /opt/tomcat
```

- `chown -R tomcat:tomcat /opt/tomcat` setzt den Besitzer und die Gruppe des `/opt/tomcat`-Verzeichnisses auf den
  tomcat-Benutzer.
- `chmod -R 754 /opt/tomcat` setzt die Berechtigungen für das `/opt/tomcat`-Verzeichnis.
    - 7 gewährt Lese-, Schreib- und Ausführungsrechte für den Besitzer.
    - 5 gewährt Lese- und Ausführungsrechte für die Gruppe.
    - 4 gewährt Leserechte für andere.

### Erstelle einen Systemd-Dienst für Tomcat 10

Erstelle einen Systemd-Dienst, um Tomcat 10 zu verwalten.
Dadurch kann Systemd den Tomcat-10-Dienst automatisch starten und stoppen.
Füge den folgenden Inhalt in die Datei ein und speichere sie.

```shell
> sudo nano /etc/systemd/system/openrouteservice.service
```

```ini
[Unit]
Description=Tomcat - openrouteservice
After=syslog.target network.target

[Service]
Type=forking

User=tomcat
Group=tomcat
RestartSec=10
Restart=always

Environment="JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64/"
Environment="JAVA_OPTS=-Djava.security.egd=file:///dev/urandom"
Environment="CATALINA_OPTS=-server -XX:+UseParallelGC"

Environment=CATALINA_PID=/opt/tomcat/temp/tomcat.pid
Environment=CATALINA_HOME=/opt/tomcat
Environment=CATALINA_BASE=/opt/tomcat

ExecStart=/opt/tomcat/bin/startup.sh
ExecStop=/opt/tomcat/bin/shutdown.sh

[Install]
WantedBy=multi-user.target
```

### Teste die neue Tomcat-10-Einrichtung

```shell
# Lade den Systemd-Daemon neu
> sudo systemctl daemon-reload
# Starte den Tomcat-10-Dienst
> sudo systemctl enable --now openrouteservice.service
# Überprüfe den Status des Tomcat-10-Dienstes
> sudo systemctl status openrouteservice.service
```

Navigiere zu [http://localhost:8080](http://localhost:8080) in deinem Browser, um die Tomcat-10-Willkommensseite zu
sehen.

## Vorbereitung der openrouteservice-Umgebung

Da Tomcat 10 nun eingerichtet ist, können wir auch openrouteservice mit Java 17 einrichten.

### Das openrouteservice WAR-File herunterladen

Um openrouteservice v8 mit Tomcat 10 einzurichten, benötigst du zunächst das entsprechende WAR-File. Besuche
die [openrouteservice Releases-Seite](https://github.com/GIScience/openrouteservice/releases) und lade die neueste
Version des WAR-Files herunter.

```shell
# Lade das neueste openrouteservice WAR-File herunter für v8.2.0.
> curl -L https://github.com/GIScience/openrouteservice/releases/download/v8.2.0/ors.war > ors.war
# Verschiebe das WAR-File in das Tomcat-Webapps-Verzeichnis
> mv ors.war /opt/tomcat/webapps/
# Starte den Tomcat-10-Dienst neu
> sudo systemctl restart openrouteservice.service
# Überprüfe den Status des Tomcat-10-Dienstes
> sudo systemctl status openrouteservice.service
```

Navigiere zu [http://localhost:8080/ors/v2/health](http://localhost:8080/ors/v2/health), um den Status von
openrouteservice zu sehen.
Die Ausgabe sollte wie folgt aussehen:

```json
{
    "status": "not ready"
}

```

::: info
ORS ist nun korrekt mit Tomcat 10 eingerichtet, muss jedoch noch konfiguriert werden. Daher zeigt es `not ready` an.
Wenn du genaue Log-Ausgaben sehen möchtest, kannst du die Tomcat-Logdateien im Verzeichnis `/opt/tomcat/logs`
überprüfen.
:::

### Die openrouteservice-Ordnerstruktur erstellen

Um openrouteservice korrekt zum Laufen zu bringen,
musst du die Ordnerstruktur einrichten,
eine Test-OSM-Datei herunterladen und die Konfiguration vornehmen.

```shell
mkdir -p "/opt/openrouteservice/graphs"
mkdir -p "/opt/openrouteservice/logs"
mkdir -p "/opt/openrouteservice/data"
mkdir -p "/opt/openrouteservice/elevation_cache"
```

::: info
Die Ordnerstruktur ist ein Vorschlag von uns und kann an deine Bedürfnisse angepasst werden.
Stelle nur sicher, dass du die Konfiguration entsprechend anpasst sowie die Berechtigungen korrekt setzt.
:::

### Eine Test-OSM-Datei herunterladen

Eine gute Quelle für aktuelle OSM-Dateien ist der [Geofabrik-Downloadserver](https://download.geofabrik.de/).
Wir werden eine kleine Test-OSM-Datei für Andorra herunterladen.

```shell
# Lade eine Test-OSM-Datei in das Verzeichnis /opt/openrouteservice/data herunter
> curl -L https://download.geofabrik.de/europe/andorra-latest.osm.pbf > /opt/openrouteservice/data/andorra-latest.osm.pbf
```

### openrouteservice konfigurieren

Die Konfiguration von Tomcat und openrouteservice erfolgt am besten durch das Setzen der Konfiguration in der Datei
`setenv.sh` im `bin`-Verzeichnis von Tomcat.

```shell
# Erstelle die Datei `setenv.sh` im `bin`-Verzeichnis von Tomcat
> sudo nano /opt/tomcat/bin/setenv.sh
```

Füge die folgenden Inhalte in die Datei ein und speichere sie.
Stelle sicher, dass du den Wert von `-Xmx` an dein System und die Größe der verwendeten `OSM-Datei` anpasst.
Wenn du eine andere `OSM-Datei` erstellen möchtest, kannst du den Wert von source_file anpassen.

Wenn du mehr über die neuen Konfigurationsoptionen in Version 8 erfahren möchtest, lies
die [Konfigurationsdokumentation](/run-instance/configuration/index.md).

**Beispiel `setenv.sh`-Datei für openrouteservice v8**

```shell
export CATALINA_OPTS="$CATALINA_OPTS -server -XX:+UseParallelGC -Xmx15g"
export JAVA_OPTS="$JAVA_OPTS \
-Dors.engine.profiles.car.enabled=true \
-Dors.engine.graphs_data_access=MMAP \
-Dors.engine.profile_default.elevation=false \
-Dors.engine.graphs_root_path=/opt/openrouteservice/graphs \
-Dors.engine.source_file=/opt/openrouteservice/data/andorra-latest.osm.pbf \
-Dlogging.file.name=/opt/openrouteservice/logs/ors.log \
-Dors.engine.elevation.cache_path=/opt/openrouteservice/elevation_cache
"
```

Setze `ors.engine.graphs_data_access` auf `RAM_STORE`, wenn du den RAM-Store anstelle des MMAP-Stores verwenden
möchtest.
Achte darauf, dass `-Xmx` an dein System und deinen Graphen angepasst ist und dein System über genügend Speicher
verfügt.


## openrouteservice ausführen

Wenn du die oben genannten Schritte befolgt hast, sollte openrouteservice nun korrekt mit Tomcat 10 und Java 17
eingerichtet sein.
Die folgenden Schritte sind immer notwendig, wenn du die Konfiguration, die Ordnerstruktur oder die Graphen änderst:

```shell
# Setze die Besitzrechte für Tomcat erneut
> sudo chown -R tomcat:tomcat /opt/tomcat
> sudo chown -R tomcat:tomcat /opt/openrouteservice
> sudo chmod -R 754 /opt/tomcat/bin/setenv.sh
> sudo chmod -R 754 /opt/openrouteservice
# Starte den Tomcat-10-Dienst neu
> sudo systemctl restart openrouteservice.service
# Überprüfe den Status des Tomcat-10-Dienstes
> sudo systemctl status openrouteservice.service
```

Nachdem openrouteservice nun korrekt mit den richtigen Konfigurationen eingerichtet ist,
überprüfe die Logdateien im Verzeichnis `/opt/openrouteservice/logs/ors.log` oder die Tomcat-Logdateien im Verzeichnis
`/opt/tomcat/logs`.
Navigiere zu [http://localhost:8080/ors/v2/health](http://localhost:8080/ors/v2/health) in deinem Browser, um den
Status von openrouteservice zu überprüfen, oder sende eine Anfrage an die API.

```shell
> curl http://localhost:8080/ors/v2/health
```

Die Ausgabe sollte wie folgt aussehen:

```json
{
    "status": "ready"
}
```

## Beispielanfragen

Teste, ob openrouteservice korrekt funktioniert, indem du eine Anfrage an die API sendest.
Passe den Test entsprechend deiner heruntergeladenen OSM-Datei an.

```shell
# Teste eine Route von A nach B in Andorra
> curl -X POST \
  'http://localhost:8080/ors/v2/directions/driving-car' \
  -H 'Content-Type: application/json; charset=utf-8' \
  -H 'Accept: application/json, application/geo+json, application/gpx+xml, img/png; charset=utf-8' \
  -d '{"coordinates":[[1.5036892890930176, 42.4972256361276],[1.6298711299896242, 42.57831077271361]]}'

# Teste eine Isochrone von einem Punkt in Andorra
> curl -X POST \
  'http://localhost:8080/ors/v2/isochrones/driving-car' \
  -H 'Content-Type: application/json; charset=utf-8' \
  -H 'Accept: application/json, application/geo+json, application/gpx+xml, img/png; charset=utf-8' \
  -d '{"locations":[[1.5036892890930176, 42.4972256361276]],"range":[300]}'
```

## Optional: Pre-Build Graphen updaten

Falls du nur die vorhandenen Graphen durch neue vorgefertigte ersetzen möchtest, folge diesen Schritten:

1. Lade die neuen Graphen von der entsprechenden Quelle herunter.
   Diese liegen in der Regel in einer `zip-` oder `tar.xz`-Datei vor.
2. Entpacke die neuen Graphen und stelle sicher, dass sie das richtige Format haben.

```shell
# Im Fall einer tar.xz-Datei
> tar -xvf new_graphs.tar.xz
# Im Fall einer ZIP-Datei kannst du den unzip-Befehl verwenden
> unzip new_graphs.zip
```

Die entpackten Graphen haben eine ähnliche Ordnerstruktur wie diese:

```shell
new_graphs/
├── driving-car
└── cycling-regular
```

3. Leere das alte Graphen-Verzeichnis `/opt/openrouteservice/graphs/`:

```shell
> mv /opt/openrouteservice/graphs /opt/openrouteservice/graphs_old
> mkdir /opt/openrouteservice/graphs
```

4. Verschiebe die neuen Graphen in das Verzeichnis `/opt/openrouteservice/graphs/`.
   Die neue Graphen-Ordnerstruktur sollte wie folgt aussehen:

```shell
/opt/openrouteservice/graphs/
├── driving-car
└── cycling-regular
```

5. Setze die [korrekten Berechtigungen](#setze-die-notwendigen-berechtigungen-fur-den-tomcat-10-benutzer) für das Tomcat-Verzeichnis und starte den
   Tomcat-Dienst neu.
6. Führe die [Beispielanfragen](#beispielanfragen) aus, um zu überprüfen, ob die neuen Graphen korrekt funktionieren.

## Optional: openrouteservice aktualisieren

Falls du openrouteservice auf eine neue Version aktualisieren möchtest, kannst du die folgenden Schritte ausführen:

1. Stoppe den Tomcat-Dienst.

```shell
> sudo systemctl stop openrouteservice.service
```

2. Lade die neue [WAR-Datei](#das-openrouteservice-war-file-herunterladen) für die gewünschte Version herunter.
3. Leere das alte Webapps-Verzeichnis `/opt/tomcat/webapps/` und verschiebe die neue `WAR-Datei` in dieses Verzeichnis.
4. Passe die [openrouteservice-Konfiguration](#openrouteservice-konfigurieren) in der Datei `setenv.sh` an.
5. [Setze die korrekten Berechtigungen](#setze-die-notwendigen-berechtigungen-fur-den-tomcat-10-benutzer) für das Tomcat-Verzeichnis und starte den
   Tomcat-Dienst neu.
6. Führe die [Beispielanfragen](#beispielanfragen) aus, um zu überprüfen, ob die neue Version korrekt funktioniert.
