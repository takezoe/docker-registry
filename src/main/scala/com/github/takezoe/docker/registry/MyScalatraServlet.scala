package com.github.takezoe.docker.registry

import com.github.takezoe.docker.registry.storage.DockerRegistryStorage
import org.scalatra._

class MyScalatraServlet extends ScalatraServlet {
  private val storage = new DockerRegistryStorage()

  get("/") {
    views.html.hello()
  }

  // API Version Check
  get("/v2/") {
    Ok()
  }

  // Pulling an Image Manifest
  get("/v2/:name/manifests/:reference") {
    NotImplemented()
  }

  // Existing Manifests
  head("/v2/:name/manifests/:reference") {
    NotImplemented()
  }

  // Pulling a Layer
  get("/v2/:name/blobs/:digest") {
    NotImplemented()
  }

  // Starting An Upload
  post("/v2/:name/blobs/uploads") {
    val name   = params("name")
    val digest = params.get("digest")

    digest match {
      case Some(digest) =>
        val in = request.getInputStream
        val uuid = storage.prepareUploadLayer()
        storage.uploadLayer(name, uuid, in)
        storage.finishUploadLayer(name, uuid, digest)
        Created()
      case None =>
        val uuid = storage.prepareUploadLayer()
        response.addHeader("Location", s"/v2/${name}/blobs/uploads/${uuid}")
        response.addHeader("Range", "0-0")
        response.addHeader("Docker-Upload-UUID", uuid)
        Accepted()
    }
  }

  // Existing Layers
  head("/v2/:name/blobs/:digest") {
  }

  // Upload Progress
  get("/v2/:name/blobs/uploads/:uuid") {
    val name = params("name")
    val uuid = params("uuid")
    val progress = storage.getProgress(uuid)
    response.addHeader("Range", s"0-${progress}")

    NoContent()
  }

  // Monolithic Upload
  put("/v2/:name/blobs/uploads/:uuid") {
    val name   = params("name")
    val uuid   = params("uuid")
    val digest = params.get("digest")

    val in = request.getInputStream
    storage.uploadLayer(name, uuid, in)

    digest match {
      case Some(digest) =>
        storage.finishUploadLayer(name, uuid, digest)
        Created()
      case None =>
        Accepted()
    }
  }

  // Chunked Upload
  patch("/v2/:name/blobs/uploads") {
    NotImplemented()
  }

  // Canceling an Upload
  delete("/v2/:name/blobs/uploads/:uuid") {

  }

  // Deleting a Layer
  delete("/v2/:name/blobs/:digest") {
    NotImplemented()
  }

  // Pushing an Image Manifest
  put("/v2/:name/manifests/:reference") {
    val in = request.getInputStream

    // TODO debug
    val bytes = new Array[Byte](in.available())
    in.read(bytes)
    println(new String(bytes, "UTF-8"))

    NotImplemented()
  }

  // Listing Repositories
  get("/v2/_catalog") {
    NotImplemented()
  }

  // Listing Image Tags
  get("/v2/:name/tags/list") {
    NotImplemented()
  }

  // Deleting an Image
  delete("/v2/:name/manifests/:reference") {
    NotImplemented()
  }
}
