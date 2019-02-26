# spring-boot-docker-build

We take a look at how to create Java and Spring Boot-based applications in a Docker container. It's gonna be a whale of a time! 

There are a lot of guides on “Docker for Java developers,” but most of them do not take care of small and efficient Docker images.

I have combined many resources on how to make a simple and fast Docker image containing any Spring Boot-like application.

My goals:

* Create a single and portable Dockerfile (as general as possible).
* Make Maven build inside Docker (no need to have Maven locally).
* Don’t download any Maven dependencies repeatedly, if no changes in pom.xml (rebuilding image as fast as possible).
* The final Docker image should contain only application itself (no source codes, no Maven dependencies required by Maven build, etc.)
* The final image should be as small as possible (no full JDK required).
* The application inside Docker should remain configurable as much as possible (with all Spring Boot configuration options).
* Possibility to enable debugging (on demand).
* Possibility to see log files.

The final image is designed for development purposes, but it does not contain any no-go production parts and it is fully configurable.

To fulfill a single portable Dockerfile requirement, I need to use Docker [multi-stage builds.](https://docs.docker.com/develop/develop-images/multistage-build/)

It will have two main parts (stages):

* The building part.
* The runtime part.

#### The Building Part of the Dockerfile

```
### BUILD image

FROM maven:3-jdk-11 as builder

# create app folder for sources

RUN mkdir -p /build

WORKDIR /build

COPY pom.xml /build

#Download all required dependencies into one layer

RUN mvn -B dependency:resolve dependency:resolve-plugins

#Copy source code

COPY src /build/src

# Build application

RUN mvn package
```

I have started from the [official Maven image](https://hub.docker.com/_/maven/), so you may change this as you wish. The most interesting part is this:

`RUN mvn -B dependency:resolve dependency:resolve-plugins`

It downloads all dependencies required either by your application or by plugins called during a build process. Then all dependencies are a part of one layer. That layer does not change until any changes are found in the pom.xml file. 

So the rebuilding is very fast and does not include downloading all the dependencies again and again.

The second option, how to download required dependencies, comes from the [official Docker Maven site](https://github.com/carlossg/docker-maven) (when you have some problems with the previous variant):

`RUN mvn -B -e -C -T 1C org.apache.maven.plugins:maven-dependency-plugin:3.0.2:go-offline`

