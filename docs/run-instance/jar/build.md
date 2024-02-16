# Build openrouteservice JAR


::: tip Hint
If you don't need changes in the code, you don't need to build the artifact yourself. If this is the case, skip this page and go to [download](download.md).
:::

First checkout the openrouteservice repository and setup your local project.
How this is done is independent of the artifact type you want to use and is documented in [Building from Source](/run-instance/building-from-source.md).
There you will also learn how to carry out tests, which is essential for code changes.

When your project is set up, you can generate a runnable openrouteservice fat JAR: 
```shell
mvn clean package -PbuildFatJar
```

Because JAR is the default, you can also run the command without `-PbuildFatJar`:
```shell
mvn clean package
```

You will find the fat JAR in  

```shell
ors-api/target/ors.jar
```

Read in the next chapters how to configure and run the jar.
