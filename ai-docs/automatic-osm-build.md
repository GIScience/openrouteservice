# 🔄 Descarga Automática de Mapas OSM en el Build

Este documento describe cómo configurar OpenRouteService para descargar automáticamente los archivos OSM durante el proceso de construcción de la imagen Docker.

## 📋 Resumen

En lugar de descargar manualmente el archivo OSM y copiarlo al directorio correcto, podemos modificar el `Dockerfile` para que descargue automáticamente el archivo durante el proceso de construcción. Esto tiene varias ventajas:

- ✅ Automatización completa del proceso de despliegue
- ✅ Aprovechamiento del sistema de capas de Docker para cachear la descarga
- ✅ No es necesario incluir archivos grandes en el control de versiones
- ✅ Ideal para despliegues en plataformas como Railway.com

## 🛠️ Configuración

### 1. Modificar el Dockerfile

Agrega estas líneas al `Dockerfile` en la sección de la imagen final:

```dockerfile
# Build ARGS
ARG UID=1000
ARG GID=1000
ARG OSM_FILE=./ors-api/src/test/files/heidelberg.test.pbf
ARG ORS_HOME=/home/ors
ARG OSM_URL=https://download.geofabrik.de/south-america/chile-latest.osm.pbf  # URL del mapa a descargar

# Setup the target system with the right user and folders.
RUN apk update && apk add --no-cache bash=~5 jq=~1 openssl=~3 wget=~1.21 && \
    addgroup ors -g ${GID} && \
    mkdir -p ${ORS_HOME}/logs ${ORS_HOME}/files ${ORS_HOME}/graphs ${ORS_HOME}/elevation_cache  && \
    adduser -D -h ${ORS_HOME} -u ${UID} --system -G ors ors  && \
    chown ors:ors ${ORS_HOME} \
    && chmod -R 777 ${ORS_HOME}

# Download OSM file and set up files
RUN mkdir -p ${ORS_HOME}/files && \
    wget -q ${OSM_URL} -O ${ORS_HOME}/files/chile-latest.osm.pbf && \
    chown ors:ors ${ORS_HOME}/files/chile-latest.osm.pbf
```

### 2. Configurar docker-compose.yml

El archivo `docker-compose.yml` debe tener la siguiente configuración:

```yaml
services:
  ors-app:
    environment:
      REBUILD_GRAPHS: "True"  # Activar para el primer despliegue
      XMS: 4g  # Memoria inicial para Java
      XMX: 8g  # Memoria máxima para Java
      ors.engine.profile_default.build.source_file: /home/ors/files/chile-latest.osm.pbf
    volumes:
      - ./ors-docker:/home/ors
      - ./ors-docker/graphs:/home/ors/graphs
      - ./ors-docker/elevation_cache:/home/ors/elevation_cache
      - ./ors-docker/config:/home/ors/config
      - ./ors-docker/logs:/home/ors/logs
      - ./ors-docker/files:/home/ors/files
```

## 🚀 Proceso de Construcción

1. **Durante el build**:
   - Docker ejecutará el comando `wget` para descargar el archivo OSM
   - La descarga se realiza en una capa específica que puede ser cacheada
   - El archivo se guarda directamente en el directorio correcto

2. **Caché de Docker**:
   - La capa que contiene el archivo OSM se cacheará
   - Builds posteriores reutilizarán la capa cacheada si no hay cambios
   - Para forzar una nueva descarga, usa `docker build --no-cache`

3. **Primer inicio**:
   - `REBUILD_GRAPHS=True` asegura que los grafos se construyan
   - Los grafos se generan usando el archivo OSM descargado
   - Los volúmenes montados persisten los grafos generados

## 📝 Notas Importantes

1. **Tamaño de memoria**:
   - Ajusta `XMS` y `XMX` según el tamaño del archivo OSM
   - Para Chile (~650MB), los valores actuales son adecuados
   - Regla: `XMX = tamaño_archivo * 2 * número_de_perfiles`

2. **Archivos ignorados por Git**:
   - Es correcto que `ors-docker/**` esté en `.gitignore`
   - Los archivos OSM y grafos no deben estar en control de versiones
   - La configuración se maneja a través de variables de entorno

3. **Cambio de región**:
   - Para usar un mapa diferente, modifica `OSM_URL` en el build:
   ```bash
   docker build --build-arg OSM_URL=https://download.geofabrik.de/europe/spain-latest.osm.pbf -t ors:spain .
   ```

## 🔍 Verificación

Para verificar que todo funciona correctamente:

1. Construye la imagen:
   ```bash
   docker compose build
   ```

2. Inicia el servicio:
   ```bash
   docker compose up -d
   ```

3. Verifica el funcionamiento:
   ```bash
   curl "http://localhost:8080/ors/v2/health"
   ```

4. Prueba una ruta:
   ```bash
   curl "http://localhost:8080/ors/v2/directions/driving-car?start=-70.6483,-33.4569&end=-70.6683,-33.4490"
