# docker-registry

Scala implementation of Docker Registry API V2

## Build & Run

```sh
$ cd docker-registry
$ sbt
> jetty:start
> browse
```

If `browse` doesn't launch your browser, manually open [http://localhost:8080/](http://localhost:8080/) in your browser.

## References

- [Docker Registry HTTP API V2](https://docs.docker.com/registry/spec/api/)
- [Deploy a registry server](https://docs.docker.com/registry/deploying/)
- [Official Docker Registry implementation](https://github.com/docker/docker-ce/tree/master/components/engine/registry)
- [Docker Registry client](https://github.com/heroku/docker-registry-client)
