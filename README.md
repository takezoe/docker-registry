# docker-registry

Scala implementation of Docker Registry API V2

## Build & Run

Run on Local

```sh
$ sbt ~jetty:start
```

Run on Docker

```sh
$ ./build-docker-image.sh
$ docker run -t -i --rm -p 8080:8080 scala-docker-registry
```

Test to push a docker image

```sh
$ docker pull ubuntu:16.04
$ docker tag ubuntu:16.04 localhost:8080/my-ubuntu
$ docker push localhost:8080/my-ubuntu
```

## References

- [Docker Registry HTTP API V2](https://docs.docker.com/registry/spec/api/)
- [Deploy a registry server](https://docs.docker.com/registry/deploying/)
- [Official Docker Registry implementation](https://github.com/distribution/distribution/tree/main/registry)
- [Docker Registry client](https://github.com/heroku/docker-registry-client)
