package com.github.takezoe.docker.registry.storage

import java.io.{File, FileOutputStream, InputStream}
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

  def uploadLayer(name: String, uuid: String, in: InputStream): Unit = {
    Using.resource(new FileOutputStream(new File(s"${dataDir}/${name}-${uuid}"), true)){ out =>
      val bytes = new Array[Byte](81024 * 8)
      var length = 0
      while({length = in.read(bytes); length != -1 }) {
        out.write(bytes, 0, length)
        progress.get(uuid).add(length)
      }
    }
  }

  def getProgress(uuid: String): Long = {
    Option(progress.get(uuid)).map(_.longValue()).getOrElse(0L)
  }

  def finishUploadLayer(name: String, uuid: String, digest: String): Unit = {
    val dir = new File(s"${dataDir}/${name}")
    if (!dir.exists()) {
      dir.mkdir()
    }
    new File(s"${dataDir}/${name}-${uuid}").renameTo(new File(s"${dataDir}/${name}/${digest}"))
    progress.remove(uuid)
  }

  def pushManifest(manifest: com.github.takezoe.docker.registry.entity.Manifest): Unit = {
    ???
  }
}
