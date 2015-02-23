package com.articlio.dataExecution.concrete
import com.articlio.dataExecution._
import util._
import com.articlio.config
import com.articlio.pipe.pipelines.ReadyJATS

/*
 *  This class handles its own dependencies, as they are disjunctive (one of the two is enough).
 *  In case this class survives further on, its handling of disjunctive dependencies may be
 *  merged into the overall object model, as a variant to existing classes.
 */

case class JATSaccess(dirPath: String) extends Access
case class JATSData(articleName: String) extends Data
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
  
  def registerDependency(data: Data, dependedOnData: Data) : Unit = {
    import scala.slick.driver.MySQLDriver.simple._
    import scala.slick.jdbc.meta._
    import models.Tables._
    import models.Tables.{Data => DataRecord}
    import com.articlio.storage.Connection
    Datadependencies += DatadependenciesRow(data.dataID.get, dependedOnData.dataID.get)
  }
  
  def create()(dataID: Long, articleName:String) : Option[CreateError] = {
    import controllers.PdfConvert
      val executionManager = new DataExecutionManager // TODO: no real reason to spawn a new execution manager just for this 
      executionManager.getSingleDataAccess(eLifeJATSDep) match {
        case access: Access => {
          ReadyJATS.fix()_
          registerDependency(this, eLifeJATSDep)
          None
        }
        case error:  AccessError => 
          executionManager.getSingleDataAccess(PDFDep) match {
            case access: Access => {
              PdfConvert.convertSingle(s"${config.config.getString("locations.pdf-input")}/$articleName")
              registerDependency(this, eLifeJATSDep)
              None
            }
            case error:  AccessError => Some(CreateError(s"disjunctive dependency for creating JATS for $articleName has not been met.")) 
          }  
    }
  }; val creator = create()_
  
  val access = JATSaccess(config.config.getString("locations.JATS")) // floggingfasdflePathExists(s"${config.eLife}/$articleName.xml") match {
}
