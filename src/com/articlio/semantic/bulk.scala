package com.articlio.semantic
import java.io.File
import com.articlio.util.runID
import com.articlio.input.JATS
import com.articlio.ldb
import com.articlio.config
import com.articlio.dataExecution._
import com.articlio.dataExecution.concrete._

class Bulk(runID: String) { 

  def processAllNew(runID: String, sourceDirName: String, pdb: String = "Normalized from July 24 2014 database - Dec 30 - plus Jan tentative addition.csv", treatAs: Option[String] = None) {
    
    val files = new File(sourceDirName).listFiles.filter(file => (file.isFile)) // && file.getName.endsWith(".xml")))
    val executionManager = new DataExecutionManager
    
    def makeOrVerify(articelName: String): Boolean = {
        executionManager.getSingleDataAccess(new SemanticData(articelName, pdb)()) match {
        case error:  AccessError =>
          //println("Result data failed to create. Please contact development with all necessary details (url, and description of what you were doing)")
          false
        case dataAccessDetail : Access =>  
          //println("Done processing file")
          true
        }
    }
    
    
    //AppActorSystem.outDB ! "startToBuffer"
    files.par.map(file => {
      val fileName = file.getName  
      println("about to process file " + fileName)
      treatAs match {
        case Some(s) => 
          //AppActorSystem.outDB ! ldb.ldb.go(runID, new JATS(s"$sourceDirName/$fileName", s))
          makeOrVerify(fileName) // TODO: run for elife vs. pdf sourced
        case None => 
          //AppActorSystem.outDB ! ldb.ldb.go(runID, new JATS(s"$sourceDirName/$fileName"))
          makeOrVerify(fileName) // TODO: run for elife vs. pdf sourced
      }
    })
    //AppActorSystem.outDB ! "flushToDB"
  }

  def allPDF = processAll(runID, config.pdf, Some("pdf-converted"))
  def alleLife = processAll(runID, config.eLife)
  
  def all {
    allPDF
    alleLife
  } 
    
    //ldb.ldb.go(new JATS("../data/ready-for-semantic/from-pdf/management 14", "pdf-converted"))
    //ldb.ldb.go(new JATS("../data/ready-for-semantic/eLife-JATS/elife03399.xml", "pdf-converted"))
    //ldb.ldb.go(new JATS("/home/matan/ingi/repos/fileIterator/data/toJATS/test", "pdf-converted"))
    //ldb.ldb.go(new JATS("/home/matan/ingi/repos/fileIterator/data/toJATS/Rayner (1998)", "pdf-converted"))
    //ldb.ldb.go(new JATS("/home/matan/ingi/repos/fileIterator/data/toJATS/imagenet", "pdf-converted"))
    //ldb.ldb.go(new JATS("/home/matan/ingi/repos/fileIterator/data/prep/elife03399.xml"))

    def processAll(runID: String, sourceDirName: String, treatAs: Option[String] = None) {
    
    val files = new File(sourceDirName).listFiles.filter(file => (file.isFile)) // && file.getName.endsWith(".xml")))
    val executionManager = new DataExecutionManager
    
    def makeOrVerify(articelName: String, pdb: String = "Normalized from July 24 2014 database - Dec 30 - plus Jan tentative addition.csv"): Boolean = {
        executionManager.getSingleDataAccess(new SemanticData(articelName, pdb)()) match {
        case error:  AccessError =>
          //println("Result data failed to create. Please contact development with all necessary details (url, and description of what you were doing)")
          false
        case dataAccessDetail : Access =>  
          //println("Done processing file")
          true
        }
    }
    
    
    //AppActorSystem.outDB ! "startToBuffer"
    files.par.map(file => {
      val fileName = file.getName  
      println("about to process file " + fileName)
      treatAs match {
        case Some(s) => 
          //AppActorSystem.outDB ! ldb.ldb.go(runID, new JATS(s"$sourceDirName/$fileName", s))
          makeOrVerify(fileName) // TODO: run for elife vs. pdf sourced
        case None => 
          //AppActorSystem.outDB ! ldb.ldb.go(runID, new JATS(s"$sourceDirName/$fileName"))
          makeOrVerify(fileName) // TODO: run for elife vs. pdf sourced
      }
    })
    //AppActorSystem.outDB ! "flushToDB"
  }
  
}
