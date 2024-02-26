# Running your own openrouteservice instance

If you need to customize the behavior of openrouteservice or if the features or quota provided by our public API is not
sufficient for your needs, you can run your own openrouteservice instance on a server or your local computer. In an own
instance, you can activate all endpoints, also those that are not available in our public API.

There are different options to achieve this. The service can be built in the form of different artifact types: **WAR**,
**JAR** or as **Docker Image**. In the first column of the table below you find a link to some basic information about
each artifact type.

No matter how you want to run your openrouteservice, you first need the corresponding artifact. The second and third
columns link to detail information about how to build a customized version yourself or where you can download the
artifact.

For configuration, operation and troubleshooting it plays no role, if you run a downloaded or customized version (as
long as you don't change basic things like configuration). You find links to information about these topics in the three
right columns.

But before you start operating your own openrouteservice in the technical way of your choice, please read the
documentation regarding [System Requirements](system-requirements) and [Data](data)!

| Artifact                               | Download                                                                | or build yourself                                        | Run                                                                 | Configure                                                  | Trouble shoot                                                    | 
|----------------------------------------|-------------------------------------------------------------------------|----------------------------------------------------------|---------------------------------------------------------------------|------------------------------------------------------------|------------------------------------------------------------------|
| [JAR](running-jar.md)                  | [Download JAR](running-jar.md#download)                                 | [Build JAR](building-from-source.md#build-jar)           | [Run JAR](running-jar.md#run)                                       | [Configure JAR](running-jar.md#configure)                  | [Troubleshoot JAR](running-jar.md#troubleshoot)                  | 
| [WAR](running-war.md)                  | [Download WAR](running-war.md#download)                                 | [Build WAR](building-from-source.md#build-war)           | [Run WAR](running-war.md#run)                                       | [Configure WAR](running-war.md#configure)                  | [Troubleshoot WAR](running-war.md#troubleshoot)                  | 
| [Docker Image](running-with-docker.md) | [Download Docker Image](running-with-docker.md#running-prebuilt-images) | [Build Docker Image](running-with-docker.md#build-image) | [Run Docker Image](running-with-docker.md#running-prebuilt-images)  | [Configure Docker Image](running-with-docker.md#configure) | [Troubleshoot Docker Image](running-with-docker.md#troubleshoot) | 
| [Source code](building-from-source.md) | [Download source code](building-from-source.md#download-source-code)    |                                                          | [Run source code](building-from-source.md#run-source-code-directly) | [Configure](building-from-source.md#configure)             |                                                                  | 

[//]: # (TODO add row for rpm package once integrated)

::: tip
We recommend to use the docker option for simplicity, or plain JAR if you do not want to install Docker.
:::

## Checking

By default, the service status can be queried via the [health endpoint](/api-reference/endpoints/health/index.md).

```shell 
curl 'http://localhost:8080/ors/v2/health'
# should result in an output like 
# {"status":"ready"}
```

When the service is ready, you will be able to request the [status endpoint](/api-reference/endpoints/status/index.md)
for further information on the running services.

```shell 
curl 'http://localhost:8080/ors/v2/status'
# should result in an output like 
# {"languages":["cs","cs-cz","de","de-de","en","en-us","eo","eo-eo","es","es-es","fr","fr-fr","gr","gr-gr","he","he-il","hu","hu-hu","id","id-id","it","it-it","ja","ja-jp","ne","ne-np","nl","nl-nl","pl","pl-pl","pt","pt-pt","ro","ro-ro","ru","ru-ru","tr","tr-tr","zh","zh-cn"],"engine":{"build_date":"2024-01-02T16:34:45Z","version":"8.0"},"profiles":{"profile 1":{"storages":{"WayCategory":{"gh_profile":"car_ors_fastest_with_turn_costs"},"HeavyVehicle":{"gh_profile":"car_ors_fastest_with_turn_costs"},"WaySurfaceType":{"gh_profile":"car_ors_fastest_with_turn_costs"},"RoadAccessRestrictions":{"gh_profile":"car_ors_fastest_with_turn_costs","use_for_warnings":"true"}},"profiles":"driving-car","creation_date":"","limits":{"maximum_distance":100000,"maximum_waypoints":50,"maximum_distance_dynamic_weights":100000,"maximum_distance_avoid_areas":100000}}},"services":["routing","isochrones","matrix","snap"]}
```

If you use the default dataset you will be able to request something like the following route request to the car profile
in Heidelberg.

```shell 
curl 'http://localhost:8080/ors/v2/directions/driving-car?start=8.681495,49.41461&end=8.687872,49.420318'
```

