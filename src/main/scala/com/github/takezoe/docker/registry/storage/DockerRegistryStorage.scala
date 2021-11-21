package com.github.takezoe.docker.registry.storage

import org.apache.commons.io.FileUtils

import java.io.{File, FileOutputStream, InputStream}
import java.nio.charset.StandardCharsets
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.LongAdder
import scala.util.Using

class DockerRegistryStorage {
  private val progress = new ConcurrentHashMap[String, LongAdder]()
  private val dataDir = "./data"

  new File(dataDir).mkdirs()

  def prepareUploadLayer(): String = {
    val uuid = UUID.randomUUID().toString
    progress.put(uuid, new LongAdder())
    uuid
  }

  def uploadLayer(name: String, uuid: String, in: InputStream): Int = {
    var size = 0
    Using.resource(new FileOutputStream(new File(s"${dataDir}/${name}-${uuid}"), true)){ out =>
      val bytes = new Array[Byte](81024 * 8)
      var length = 0
      while({length = in.read(bytes); length != -1 }) {
        out.write(bytes, 0, length)
        size = size + length
        progress.get(uuid).add(length)
      }
    }
    size
  }

  def getProgress(uuid: String): Long = {
    Option(progress.get(uuid)).map(_.longValue()).getOrElse(0L)
  }

  def getLayer(name: String, digest: String): Option[File] = {
    val file = new File(s"${dataDir}/${name}/${digest}")
    if(file.exists()) {
      Some(file)
    } else {
      None
    }
  }

  def finishUploadLayer(name: String, uuid: String, digest: String): Unit = {
    val dir = new File(s"${dataDir}/${name}")
    if (!dir.exists()) {
      dir.mkdir()
    }
    new File(s"${dataDir}/${name}-${uuid}").renameTo(new File(s"${dataDir}/${name}/${digest}"))
    progress.remove(uuid)
  }

  def deleteLayer(name: String, digest: String): Unit = {
    val file = new File(s"${dataDir}/${name}/${digest}")
    if (file.exists()) {
      file.delete()
    }
  }

  def publishManifest(name: String, reference: String, manifest: String): Unit = {
    val dir = new File(s"${dataDir}/${name}")
    if (!dir.exists()) {
      dir.mkdir()
    }
    val file = new File(s"${dataDir}/${name}/${reference}.json")
    FileUtils.writeByteArrayToFile(file, manifest.getBytes(StandardCharsets.UTF_8))
  }

  def deleteManifest(name: String, reference: String): Unit = {
    val file = new File(s"${dataDir}/${name}/${reference}.json")
    if (file.exists()) {
      file.delete();
    }
  }

//  def pushManifest(manifest: com.github.takezoe.docker.registry.entity.Manifest): Unit = {
//    ???
//  }
}
