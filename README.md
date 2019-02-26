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

#### How to Customize Maven Settings

There are many situations where you need to change a default Maven setting for your customized build. To do that, you need to copy your settings.xml file into the image before you provide the builder image definition, For example:
```
FROM maven:3-jdk-11 as builder

#Copy Custom Maven settings

COPY settings.xml /root/.m2/
```

#### The Runtime Part of the Dockerfile
```
FROM openjdk:11-slim as runtime

EXPOSE 8080

#Set app home folder

ENV APP_HOME /app

#Possibility to set JVM options (https://www.oracle.com/technetwork/java/javase/tech/vmoptions-jsp-140102.html)

ENV JAVA_OPTS=""

#Create base app folder

RUN mkdir $APP_HOME

#Create folder to save configuration files

RUN mkdir $APP_HOME/config

#Create folder with application logs

RUN mkdir $APP_HOME/log

VOLUME $APP_HOME/log

VOLUME $APP_HOME/config

WORKDIR $APP_HOME

#Copy executable jar file from the builder image

COPY --from=builder /build/target/*.jar app.jar

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar" ]

#Second option using shell form:

#ENTRYPOINT exec java $JAVA_OPTS -jar app.jar $0 $@
```

The runtime part starts with some necessary steps, i.e. exposing ports, setting up environments, and creating some useful folders. The most interesting part is related to copying a previously created jar file into our new image:
```
#Copy executable jar file from the builder image

COPY --from=builder /build/target/*.jar app.jar
```

I am copying from the builder image, see the param –from. For more information about copying files from other images, see the [Docker documentation page.](https://docs.docker.com/engine/reference/commandline/cp/)     

As for the Spring Boot application, the created jar file is executable, so it is possible to run our application with the single command:

`ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar" ]`

To reduce [Tomcat startup time](https://wiki.apache.org/tomcat/HowTo/FasterStartUp#Entropy_Source) there is a system property pointing to /dev/urandom.

There are other options for runing a Spring Boot application inside Docker. For more info, visit [the official Spring guide.](https://spring.io/guides/gs/spring-boot-docker/)

### How to Build and Run Spring Boot Application in Docker in One Step
```
docker build -t <image_tag> . && docker run -p 8080:8080 <image_tag>
```
The above command will build your application with Maven and start it without any delay. This is the simplest way without any customizations. The file may come with some specific requirements, so here’s a couple of them.

Now you can visit the URL to get response from [my GitHub example:](https://github.com/hendisantika/spring-boot-docker-build.git)

http://localhost:8081/customer/10