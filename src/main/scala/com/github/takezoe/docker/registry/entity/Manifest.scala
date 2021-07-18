package com.github.takezoe.docker.registry.entity

case class Manifest(name: String, architecture: String, tag: String, fsLayers: Seq[FSLayer], history: Seq[History], schemaVersion: Int, signatures: Signatures)
case class FSLayer(blobSum: String)
case class History(v1Compatibility: String)
case class Signatures(header: Map[String, Any], signature: String, `protected`: String)
