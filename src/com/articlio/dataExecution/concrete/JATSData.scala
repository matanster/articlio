package com.articlio.dataExecution.concrete
import com.articlio.dataExecution._
import util._
import com.articlio.config
import com.articlio.pipe.pipelines.ReadyJATS
import scala.concurrent.Await
import scala.concurrent.Promise
import scala.concurrent.Future
import scala.concurrent.duration._

/*
 *  This class handles its own dependencies, as they are disjunctive (one of the two is enough).
 *  In case this class survives further on, its handling of disjunctive dependencies may be
 *  merged into the overall object model, as a variant to existing classes.
 */

case class JATSaccess(dirPath: String) extends Access
case class JATSData(articleName: String) extends DataObject
{
  val dataType = "JATS"
  
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
    import scala.slick.driver.MySQLDriver.simple._
    import scala.slick.jdbc.meta._
    import models.Tables._
    import models.Tables.{Data => DataRecord}
    import com.articlio.storage.Connection
    Datadependencies += DatadependenciesRow(data.dataID.get, dependedOnData.dataID.get)
  }
  
  def create()(dataID: Long, dataTopic: String, articleName:String) : Option[CreateError] = {
    
    def convertSingle(articleName: String) : Future[Boolean] = {
      import play.api.libs.ws.WS
      import play.api.Play.current
      implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
      WS.url("http://localhost:3000/handleInputFile").withQueryString("localLocation" -> articleName).get.map(response =>
        response.status match { 
          case 200 => true  //Ok("successfully converted pdf to JATS")
          case _   => false //InternalServerError("failed converting pdf to JATS")
        })
    }
    
    import controllers.PdfConvert
    import play.api.mvc._
    com.articlio.util.Console.log("in JATS create", "green")
    val executionManager = new DataExecutionManager // TODO: no real reason to spawn a new execution manager just for this 
    executionManager.getSingleDataAccess(eLifeJATSDep) match {
      case access: Access => {
        com.articlio.util.Console.log("before JATS convert/clean convertttttttttttttt", "green")
        ReadyJATS.fix()_
        registerDependency(this, eLifeJATSDep)
        None
      }
      case error:  AccessError => 
        executionManager.getSingleDataAccess(PDFDep) match {
          case access: Access => {
            com.articlio.util.Console.log("before pdf convertttttttttttttt", "green")
            Await.result(convertSingle(s"${config.config.getString("locations.pdf-source-input")}/$articleName"), 10.seconds) match {
              case true => registerDependency(this, PDFDep); None
              case false => Some(CreateError(s"Failed to convert pdf to JATS"))
            }
          }
          case error:  AccessError => Some(CreateError(s"disjunctive dependency for creating JATS for $articleName has not been met.")) 
        }  
    }
  }; val creator = create()_
  
  val access = JATSaccess(config.config.getString("locations.JATS")) // floggingfasdflePathExists(s"${config.eLife}/$articleName.xml"))))) match {
}
