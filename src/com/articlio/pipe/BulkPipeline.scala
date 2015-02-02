package com.articlio.pipe

//
// Some mix and match of file IO utility functions here
//
import java.io.{File}
import java.nio.file.{Path, Paths, Files}
import org.apache.commons.io.FileUtils.{deleteDirectory}
import scala.io.Source
import java.nio.charset.StandardCharsets

import sys.process._ // for being able to issue OS commands

//
// Takes an input directory, and transforms each file into a new directory.
// The idea is that this can be extended to create a whole pipeline of transformation, 
// while making every step traceable - as each transformation's output has its own directory
// and the file name remains the same across all directories.
//

package object util {  

  def writeOutputFile(fileText: String, outDir: String, fileName: String) {
    Files.write(Paths.get(outDir + "/" + fileName), fileText.getBytes(StandardCharsets.UTF_8))
  }

  def based(dir: String) = /* "../data" + "/" + */ dir

  def copy(patternOrFile: String, to: String) {
     Seq("bash", "-c", s"cp $patternOrFile ${based(to)}").!! // bash is needed for expanding the * before calling ls, ls alone doesn't do it.
  }

}

case class Step(from: String, 
                to: String, 
                transformation: (String, String, String) => Unit, 
                initializer: (String) => Unit)

class BulkPipeline(steps: Seq[Step]) {
  
  import util._

  //
  // take care of a ready empty target subfolder for a step - prefix path must already exist
  //
  def createDir(step: Step) = {
    val targetDirName: String = step.to 
    val targetDirObj = Paths.get(based(targetDirName))
    if (Files.exists(targetDirObj)) deleteDirectory(new File(based(targetDirName)))
    Files.createDirectory(targetDirObj)
  }

  //
  // take care of a ready target path for a step
  //
  def createDirRecursive(step: Step) = {
    val targetDirName: String = step.to 
    
    def impl(path: String) {
      val folderObj = Paths.get(based(path))
      if (!Files.exists(folderObj)) {
      val (prefix, last) = path.splitAt(path.lastIndexOf('/'))
      impl(prefix)
      Files.createDirectory(folderObj)
      } 
    }
    
    impl(targetDirName)
  }
  
  def StepDo(step: Step) {
    // see http://www.scala-lang.org/api/current/index.html#scala.sys.process.package for the way this invokes an OS command
    val sourceDirName = based(step.from)
    val sourceDir = new File(based(step.from))
    val targetDirName = based(step.to)

    val files = sourceDir.listFiles.filter(file => (file.isFile)) // && file.getName.endsWith(".xml")))

    step.initializer(step.to)

    // running concurrently via .par - scala will employ some parallelism by multithreding, matching the number of free cores
    files.par.foreach (file => {
      val fileName = file.getName
      step.transformation(sourceDirName, targetDirName, fileName)
    })
  }

  steps.foreach(createDirRecursive)
  steps foreach StepDo

}
