package com.github.takezoe.docker.registry

import org.scalatra.test.scalatest._

class DockerRegistryServletTests extends ScalatraFunSuite {

  addServlet(classOf[DockerRegistryServlet], "/*")

  test("GET / on MyScalatraServlet should return status 200") {
    get("/") {
      status should equal (200)
    }
  }

}
