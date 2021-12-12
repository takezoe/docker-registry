package com.github.takezoe.docker.registry

case class Catalog(repositories: Seq[String])

case class Error(code: String, message: String, detail: String)

case class Tags(name: String, tags: Seq[String])