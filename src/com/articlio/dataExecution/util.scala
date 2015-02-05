package com.articlio.dataExecution

package object util {
  def filePathExists(filePath: String) : Boolean = {
    import java.nio.file.{Paths, Files}
    Files.exists(Paths.get(filePath))
  }
}