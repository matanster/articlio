package com.articlio.dataExecution.concrete
import com.articlio.dataExecution._
import util._
import com.articlio.config
import com.articlio.pipelines.concrete.ReadyJATS
import scala.concurrent.Await
import scala.concurrent.Promise
import scala.concurrent.Future
import scala.concurrent.duration._
import com.articlio.Globals.{db, dataFilesRoot}
import com.articlio.storage.ManagedDataFiles._

/*
 *  Classes for creating JATS from various raw inputs 
 */

case class JATSaccess(dirPath: String) extends Access 
abstract class JATSData extends DataObject { val access : JATSaccess }

//
// this class handles its own dependencies, as they are disjunctive (one of the two is enough).
// In case this class survives further on, its handling of disjunctive dependencies may be
// merged into the overall object model, as a variant to existing classes.
//
case class JATSDataDisjunctiveSourced(articleName: String) extends JATSData
{
  // TODO: uber-factor this function, to rely on a function that manages collision in the case of duplicate sources for the same articleName (selects one source, or returns error)
  
  val dataTopic = articleName
  
  val dependsOn = Seq() // this data type has an "any of" relationship between its dependencies, 
                        // which is managed here internally by it rather than by the execution manager.
                        // a bit of a "design smell", may be refactored.
  //
  // generate clean JATS, from either pdf source, or eLife JATS source, whichever exists for the requested article name.
  //
  val PDFDep       = RawPDF(articleName)
  val eLifeJATSDep = RaweLifeJATS(articleName)
  
  def registerDependency(data: DataObject, dependedOnData: DataObject) : Unit = {
    import slick.driver.MySQLDriver.api._
    //import slick.jdbc.meta._
    import models.Tables._
    import models.Tables.{Data => DataRecord}
    import com.articlio.storage.Connection
    Datadependencies += DatadependenciesRow(data.successfullyCompletedID, dependedOnData.successfullyCompletedID)
  }
  
  def creator(dataID: Long, dataTopic: String, articleName:String) : Future[Option[CreateError]] = {
    
    // TODO: merge common core with same named method in controllers.
    def convertSingle(articleName: String) : Future[Option[CreateError]] = {
      import play.api.libs.ws.WS
      import play.api.Play.current
      implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
      WS.url("http://localhost:3000/handleInputFile").withQueryString("localLocation" -> s"$articleName")
                                                                       .get.map(response =>
        response.status match { 
          case 200 => None  //Ok("successfully converted pdf to JATS")
          case _   => Some(CreateError(response.body)) //InternalServerError("failed converting pdf to JATS")
        })
    }
    
    implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
    FinalData(eLifeJATSDep) flatMap {_.error match {
      case None => {
        ReadyJATS.fix()_
        registerDependency(this, eLifeJATSDep)
        Future.successful(None)
      }
      case Some(error) =>
        FinalData(PDFDep) map {f => println("f is " + f); f.error match {
          case None => {
            com.articlio.util.Console.log(s"delegating pdf conversion to node.js ($articleName)")
            Await.result(convertSingle(s"${config.config.getString("locations.pdf-source-input")}/$articleName".rooted), 10.seconds) match {
              case None => registerDependency(this, PDFDep); None
              case Some(error) => Some(CreateError(s"failed to convert pdf to JATS - response from http service was: ${error.errorDetail}"))
            }
          }
          case Some(error) => Some(CreateError(s"disjunctive dependency for creating JATS for $articleName has not been met")) 
        }}
    }}
  }
  
  val access = JATSaccess(config.config.getString("locations.JATS").rooted) // PathExists(s"${config.eLife}/$articleName.xml"))))) match {
}

//
// Create JATS naively, from raw file of mere lines of text
//
case class JATSDataFromTxtFile(articleName: String)(rawTxt: RawTxtFile = RawTxtFile(articleName)) extends JATSData
{
  val dataTopic = articleName
  
  val dependsOn = Seq(rawTxt)
  
  def creator(dataID: Long, dataTopic: String, articleName:String) : Future[Option[CreateError]] = {
    
    def writeOutputFile(fileText: String, outDir: String, fileName: String) { // TODO: move someplace more general
      //import java.io.{File}
      import java.nio.file.{Path, Paths, Files}
      import java.nio.charset.StandardCharsets
      //import scala.io.Source

      Files.write(Paths.get(outDir + "/" + fileName), fileText.getBytes(StandardCharsets.UTF_8))
    }
    
    def XMLescape(text: String) : String = { // TODO: move someplace more general
      text.replace("&","&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&apos;")
    }

    val fullSourcePath = s"${config.config.getString("locations.txtFile-source-input")}/$articleName.txt".rooted
    val targetPath = s"${config.config.getString("locations.JATS")}".rooted
    implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
    Future { // non-blocking with java.nio is rather complex, this dispatched future will block
      filePathExists(fullSourcePath) match {
        case false => Some(CreateError(s"source txt file $articleName was not found.")) 
        case true  => { 
          //val sections = scala.io.Source.fromFile(fullSourcePath).getLines.toArray.map(line => println("<sec sec-type=unknown><title>unknown</title>" + "<p>" + XMLescape(line.toString) + "</p>" + "</sec>"))
          val jats : String = 
                     "<article>" + 
                       "<body>" + 
                         scala.io.Source.fromFile(fullSourcePath).getLines.toArray.map(line =>  
                           "<sec sec-type=\"unknown\"><title>unknown</title>" + "<p>" + XMLescape(line.toString) + "</p>" + "</sec>").mkString + 
                       "</body>" + 
                     "</article>"
          writeOutputFile(jats, targetPath, s"$articleName.xml")
          implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
          None
        }
      }
    }
  }
  
  val access = JATSaccess(config.config.getString("locations.JATS").rooted) // PathExists(s"${config.eLife}/$articleName.xml"))))) match {
}

