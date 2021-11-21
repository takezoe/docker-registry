package com.github.takezoe.docker.registry

import com.github.takezoe.docker.registry.entity.Tags
import com.github.takezoe.docker.registry.storage.DockerRegistryStorage
import org.apache.commons.io.{FileUtils, IOUtils}
import org.scalatra._
import org.scalatra.json._
import org.json4s.DefaultFormats
import org.json4s.JsonAST.JString

import java.io.File
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import javax.servlet.http.HttpServletRequest
import javax.servlet.{ServletRequest, ServletResponse}

class MyScalatraServlet extends ScalatraServlet with JacksonJsonSupport {
  protected implicit lazy val jsonFormats = DefaultFormats
  private val storage = new DockerRegistryStorage()

  get("/") {
    Ok()
  }

  // API Version Check
  get("/v2/") {
    println(s"${request.getMethod} ${request.getRequestURI}")
    contentType = "application/vnd.docker.distribution.manifest.v2+json"
    response.addHeader("Docker-Distribution-Api-Version", "registry/2.0")
    Ok(Map.empty)
  }

  // Pulling an Image Manifest
  get("/v2/:name/manifests/:reference") {
    //contentType = formats("json")
    contentType = "application/vnd.docker.distribution.manifest.v2+json"
    response.addHeader("Docker-Distribution-Api-Version", "registry/2.0")
    //println("Accept: " + request.header("Accept"))
    val name      = params("name")
    val reference = params("reference")
    val file      = new File(s"data/${name}/${reference}.json")
    if (file.exists()) {
      Ok(file)
    } else {
      NotFound()
    }
  }

  // Existing Manifests
  head("/v2/:name/manifests/:reference") {
    contentType = "application/vnd.docker.distribution.manifest.v2+json"
    response.addHeader("Docker-Distribution-Api-Version", "registry/2.0")
    val name      = params("name")
    val reference = params("reference")
    val file      = new File(s"data/${name}/${reference}.json")
    if (file.exists()) {
      Ok(headers = Map("Content-Length" -> file.length().toString))
    } else {
      NotFound()
    }
  }

  // Pulling a Layer
  get("/v2/:name/blobs/:digest") {
    val name   = params("name")
    val digest = params("digest")

    storage.getLayer(name, digest) match {
      case Some(file) => Ok(file)
      case None       => NotFound()
    }
  }

  // Starting An Upload
  post("/v2/:name/blobs/uploads/") {
    val name   = params("name")
    val digest = params.get("digest")
    println(s"Digest: ${digest}")

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
    response.addHeader("Docker-Distribution-Api-Version", "registry/2.0")
    val name   = params("name")
    val digest = params("digest")
    storage.getLayer(name, digest) match {
      case Some(file) => {
        Ok(headers = Map(
          "Content-Length"        -> s"${file.length()}",
          "Docker-Content-Digest" -> digest
        ))
      }
      case None => NotFound()
    }
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
  patch("/v2/:name/blobs/uploads/:uuid") {
    val name = params("name")
    val uuid = params("uuid")

    // TODO
    val range  = request.getHeader("Content-Range")
    val length = request.getHeader("Content-Length")
    println(s"Content-Range: ${range}")
    println(s"Content-Length: ${length}")

    val in = request.getInputStream
    val size = storage.uploadLayer(name, uuid, in)

    println(s"Uploaded size: ${size}")

    response.addHeader("Docker-Upload-UUID", uuid)
    response.addHeader("Location", s"/v2/${name}/blobs/uploads/${uuid}")
    response.addHeader("Content-Length", "0")
    response.addHeader("Range", s"0-${size}")

    Accepted()
  }

  // Canceling an Upload
  delete("/v2/:name/blobs/uploads/:uuid") {
    // TODO
  }

  // Deleting a Layer
  delete("/v2/:name/blobs/:digest") {
    NotImplemented()
  }

  // Pushing an Image Manifest
  put("/v2/:name/manifests/:reference") {
    val name      = params("name")
    val reference = params("reference")
    val bytes     = IOUtils.toByteArray(request.getInputStream)
    val str       = new String(bytes, "UTF-8")

    // TODO Move this to storage
    FileUtils.writeByteArrayToFile(new File(s"./data/${name}/${reference}.json"), str.getBytes("UTF-8"))
    FileUtils.writeByteArrayToFile(new File(s"./data/${name}/sha256:${sha256(str)}.json"), str.getBytes("UTF-8"))

    val json   = parse(str)
    val digest = (json \ "config" \ "digest").asInstanceOf[JString].values

    response.addHeader("Location", s"/v2/${name}/manifests/${reference}")
    response.addHeader("Docker-Content-Digest", digest)

    Created()
  }

  // Listing Repositories
  get("/v2/_catalog") {
    contentType = formats("json")
    response.addHeader("Docker-Distribution-Api-Version", "registry/2.0")
    // {"repositories":["ubuntu"]}
    NotImplemented()
  }

  // Listing Image Tags
  get("/v2/:name/tags/list") {
    contentType = formats("json")
    response.addHeader("Docker-Distribution-Api-Version", "registry/2.0")

    val name = params("name")

    // TODO Move this to storage
    val tags = new File(s"data/$name")
      .listFiles((file, name) => name.endsWith(".json"))
      .map { file =>
        file.getName.replace("\\.json$", "")
      }

    // {"name":"ubuntu","tags":["latest"]}
    Ok(Tags(name, tags))
  }

  // Deleting an Image
  delete("/v2/:name/manifests/:reference") {
    NotImplemented()
  }

  override def service(req: ServletRequest, res: ServletResponse): Unit = {
    println(s"${req.asInstanceOf[HttpServletRequest].getMethod} ${req.asInstanceOf[HttpServletRequest].getRequestURI}")
    super.service(req, res)
  }

  private def sha256(value: String): String = {
    val digest = MessageDigest.getInstance("SHA-256")
    val bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8))
    bytesToHex(bytes)
  }

  private def bytesToHex(hash: Array[Byte]) = {
    val hexString = new StringBuilder(2 * hash.length)
    for (i <- 0 until hash.length) {
      val hex = Integer.toHexString(0xff & hash(i))
      if (hex.length == 1) hexString.append('0')
      hexString.append(hex)
    }
    hexString.toString
  }
}
