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
