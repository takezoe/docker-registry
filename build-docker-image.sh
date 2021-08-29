#!/bin/sh
sbt clean executable
docker build -t scala-docker-registry .
