package com.articlio.util
import com.articlio.config
import java.nio.file.{Paths, Files, StandardOpenOption}
import java.nio.charset.StandardCharsets

//
// Semantically routes messages to destinations, currently only to local file destinations
//
class Logger (name: String) {
  
  val base = config.output
  
  if (!Files.exists(Paths.get(base))) Files.createDirectory(Paths.get(base)) // create host folder if it doesn't yet exist
  
  def based(dir: String) = base + "/" + dir
  
  //
  // take care of a ready empty target directory - move out to util package
  //
  def createDir(targetDirName: String) {
    import java.io.{File}
    import java.nio.file.{Path, Paths, Files}
    import org.apache.commons.io.FileUtils.{deleteDirectory}
    
    
    val targetDirObj = Paths.get(based(targetDirName))
    if (Files.exists(targetDirObj)) deleteDirectory(new File(based(targetDirName)))
    Files.createDirectory(targetDirObj)
  }
  
  createDir(name)
  println(s"opening logger: $name")
  
  val openFiles = scala.collection.mutable.Map.empty[String, java.nio.file.Path]

  // 
  // initiates a file destination for given type
  //
  private def initializeType(msgType:String): java.nio.file.Path =
  {
    val fileName = based(name) + "/"+ msgType + ".out"
    val file = Paths.get(fileName)
    Files.deleteIfExists(file)
    file
  }

  //
  // writes message - see http://docs.oracle.com/javase/7/docs/api/java/nio/file/Files.html
  // TODO: consider optimizing via the buffered writer class therein or per https://docs.oracle.com/javase/tutorial/essential/io/file.html
  //
  def write(message:String, msgType:String) = {
    if (!openFiles.contains(msgType)) openFiles(msgType) = initializeType(msgType)
    val bytes = message + "\n"
    Files.write(openFiles(msgType), bytes.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.APPEND) // buffered writing may be more performant than this... see java.nio.file...
  }
}
