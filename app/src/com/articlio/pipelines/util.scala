package com.articlio.pipelines

//
// Some mix and match of file IO utility functions here
//
import java.io.{File}
import java.nio.file.{Path, Paths, Files}
import org.apache.commons.io.FileUtils.{deleteDirectory}
import scala.io.Source
import java.nio.charset.StandardCharsets
import sys.process._ // for being able to issue OS commands

object util {  

  def copyFile(fileText: String, outDir: String, fileName: String) {
    Files.write(Paths.get(outDir + "/" + fileName), fileText.getBytes(StandardCharsets.UTF_8))
  }

  
  def writeOutputFile(fileText: String, outDir: String, fileName: String) {
    Files.write(Paths.get(outDir + "/" + fileName), fileText.getBytes(StandardCharsets.UTF_8))
  }

  def based(dir: String) = dir

  def copy(patternOrFile: String, to: String) {
     Seq("bash", "-c", s"cp $patternOrFile ${based(to)}").!! // bash is needed for expanding the * before calling ls, ls alone doesn't do it.
  }

}