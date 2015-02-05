package com.articlio.dataExecution

package object util {
  def filePathExists(filePath: String) {
    import java.nio.file.{Paths, Files}
    Files.exists(Paths.get(filePath))
  }
}