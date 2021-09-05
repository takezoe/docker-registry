#!/bin/sh
sbt clean package
docker build -t scala-docker-registry .
