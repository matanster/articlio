package models
// AUTO-GENERATED Slick data model
/** Stand-alone Slick data model for immediate use */
object Tables extends {
  val profile = scala.slick.driver.MySQLDriver
} with Tables

/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.) */
trait Tables {
  val profile: scala.slick.driver.JdbcProfile
  import profile.simple._
  import scala.slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import scala.slick.jdbc.{GetResult => GR}
  
  /** DDL for all tables. Call .create to execute. */
  lazy val ddl = Abstract.ddl ++ Autoproperties.ddl ++ Bulkdatagroups.ddl ++ Data.ddl ++ Datagroupings.ddl ++ Diffs.ddl ++ Grading.ddl ++ Headers.ddl ++ Matches.ddl ++ Pdffonts.ddl ++ Pdfmeta.ddl ++ Pdftohtml.ddl ++ Runids.ddl ++ Runs.ddl ++ Runstodelete.ddl ++ Sentences.ddl ++ Title.ddl
  
  /** Entity class storing rows of table Abstract
   *  @param `abstract` Database column abstract DBType(VARCHAR), Length(20000,true), Default(None)
   *  @param docname Database column docName DBType(VARCHAR), Length(255,true), Default(None)
   *  @param runid Database column runID DBType(VARCHAR), Length(255,true), Default(None) */
  case class AbstractRow(`abstract`: Option[String] = None, docname: Option[String] = None, runid: Option[String] = None)
  /** GetResult implicit for fetching AbstractRow objects using plain SQL queries */
  implicit def GetResultAbstractRow(implicit e0: GR[Option[String]]): GR[AbstractRow] = GR{
    prs => import prs._
    AbstractRow.tupled((<<?[String], <<?[String], <<?[String]))
  }
  /** Table description of table abstract. Objects of this class serve as prototypes for rows in queries.
   *  NOTE: The following names collided with Scala keywords and were escaped: abstract */
  class Abstract(_tableTag: Tag) extends Table[AbstractRow](_tableTag, "abstract") {
    def * = (`abstract`, docname, runid) <> (AbstractRow.tupled, AbstractRow.unapply)
    
    /** Database column abstract DBType(VARCHAR), Length(20000,true), Default(None)
     *  NOTE: The name was escaped because it collided with a Scala keyword. */
    val `abstract`: Column[Option[String]] = column[Option[String]]("abstract", O.Length(20000,varying=true), O.Default(None))
    /** Database column docName DBType(VARCHAR), Length(255,true), Default(None) */
    val docname: Column[Option[String]] = column[Option[String]]("docName", O.Length(255,varying=true), O.Default(None))
    /** Database column runID DBType(VARCHAR), Length(255,true), Default(None) */
    val runid: Column[Option[String]] = column[Option[String]]("runID", O.Length(255,varying=true), O.Default(None))
  }
  /** Collection-like TableQuery object for table Abstract */
  lazy val Abstract = new TableQuery(tag => new Abstract(tag))
  
  /** Entity class storing rows of table Autoproperties
   *  @param propname Database column propName DBType(VARCHAR), Length(255,true), Default(None)
   *  @param propvalue Database column propValue DBType(VARCHAR), Length(255,true), Default(None)
   *  @param docname Database column docName DBType(VARCHAR), Length(255,true), Default(None)
   *  @param runid Database column runID DBType(VARCHAR), Length(255,true), Default(None) */
  case class AutopropertiesRow(propname: Option[String] = None, propvalue: Option[String] = None, docname: Option[String] = None, runid: Option[String] = None)
  /** GetResult implicit for fetching AutopropertiesRow objects using plain SQL queries */
  implicit def GetResultAutopropertiesRow(implicit e0: GR[Option[String]]): GR[AutopropertiesRow] = GR{
    prs => import prs._
    AutopropertiesRow.tupled((<<?[String], <<?[String], <<?[String], <<?[String]))
  }
  /** Table description of table autoProperties. Objects of this class serve as prototypes for rows in queries. */
  class Autoproperties(_tableTag: Tag) extends Table[AutopropertiesRow](_tableTag, "autoProperties") {
    def * = (propname, propvalue, docname, runid) <> (AutopropertiesRow.tupled, AutopropertiesRow.unapply)
    
    /** Database column propName DBType(VARCHAR), Length(255,true), Default(None) */
    val propname: Column[Option[String]] = column[Option[String]]("propName", O.Length(255,varying=true), O.Default(None))
    /** Database column propValue DBType(VARCHAR), Length(255,true), Default(None) */
    val propvalue: Column[Option[String]] = column[Option[String]]("propValue", O.Length(255,varying=true), O.Default(None))
    /** Database column docName DBType(VARCHAR), Length(255,true), Default(None) */
    val docname: Column[Option[String]] = column[Option[String]]("docName", O.Length(255,varying=true), O.Default(None))
    /** Database column runID DBType(VARCHAR), Length(255,true), Default(None) */
    val runid: Column[Option[String]] = column[Option[String]]("runID", O.Length(255,varying=true), O.Default(None))
  }
  /** Collection-like TableQuery object for table Autoproperties */
  lazy val Autoproperties = new TableQuery(tag => new Autoproperties(tag))
  
  /** Entity class storing rows of table Bulkdatagroups
   *  @param bulkid Database column bulkID DBType(INT), PrimaryKey
   *  @param dataid Database column dataID DBType(BIGINT) */
  case class BulkdatagroupsRow(bulkid: Int, dataid: Long)
  /** GetResult implicit for fetching BulkdatagroupsRow objects using plain SQL queries */
  implicit def GetResultBulkdatagroupsRow(implicit e0: GR[Int], e1: GR[Long]): GR[BulkdatagroupsRow] = GR{
    prs => import prs._
    BulkdatagroupsRow.tupled((<<[Int], <<[Long]))
  }
  /** Table description of table BulkDataGroups. Objects of this class serve as prototypes for rows in queries. */
  class Bulkdatagroups(_tableTag: Tag) extends Table[BulkdatagroupsRow](_tableTag, "BulkDataGroups") {
    def * = (bulkid, dataid) <> (BulkdatagroupsRow.tupled, BulkdatagroupsRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (bulkid.?, dataid.?).shaped.<>({r=>import r._; _1.map(_=> BulkdatagroupsRow.tupled((_1.get, _2.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))
    
    /** Database column bulkID DBType(INT), PrimaryKey */
    val bulkid: Column[Int] = column[Int]("bulkID", O.PrimaryKey)
    /** Database column dataID DBType(BIGINT) */
    val dataid: Column[Long] = column[Long]("dataID")
  }
  /** Collection-like TableQuery object for table Bulkdatagroups */
  lazy val Bulkdatagroups = new TableQuery(tag => new Bulkdatagroups(tag))
  
  /** Entity class storing rows of table Data
   *  @param dataid Database column dataID DBType(BIGINT), AutoInc, PrimaryKey
   *  @param datatype Database column dataType DBType(VARCHAR), Length(45,true)
   *  @param datatopic Database column dataTopic DBType(VARCHAR), Length(45,true)
   *  @param creationstatus Database column creationStatus DBType(VARCHAR), Length(45,true)
   *  @param creationerrordetail Database column creationErrorDetail DBType(VARCHAR), Length(10000,true), Default(None)
   *  @param creatorserver Database column creatorServer DBType(VARCHAR), Length(45,true)
   *  @param creatorserverstarttime Database column creatorserverstarttime DBType(TIMESTAMP), Default(None)
   *  @param creatorserverendtime Database column creatorserverendtime DBType(TIMESTAMP), Default(None)
   *  @param datadependedon Database column dataDependedOn DBType(BIGINT), Default(None) */
  case class DataRow(dataid: Long, datatype: String, datatopic: String, creationstatus: String, creationerrordetail: Option[String] = None, creatorserver: String, creatorserverstarttime: Option[java.sql.Timestamp] = None, creatorserverendtime: Option[java.sql.Timestamp] = None, datadependedon: Option[Long] = None)
  /** GetResult implicit for fetching DataRow objects using plain SQL queries */
  implicit def GetResultDataRow(implicit e0: GR[Long], e1: GR[String], e2: GR[Option[String]], e3: GR[Option[java.sql.Timestamp]], e4: GR[Option[Long]]): GR[DataRow] = GR{
    prs => import prs._
    DataRow.tupled((<<[Long], <<[String], <<[String], <<[String], <<?[String], <<[String], <<?[java.sql.Timestamp], <<?[java.sql.Timestamp], <<?[Long]))
  }
  /** Table description of table Data. Objects of this class serve as prototypes for rows in queries. */
  class Data(_tableTag: Tag) extends Table[DataRow](_tableTag, "Data") {
    def * = (dataid, datatype, datatopic, creationstatus, creationerrordetail, creatorserver, creatorserverstarttime, creatorserverendtime, datadependedon) <> (DataRow.tupled, DataRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (dataid.?, datatype.?, datatopic.?, creationstatus.?, creationerrordetail, creatorserver.?, creatorserverstarttime, creatorserverendtime, datadependedon).shaped.<>({r=>import r._; _1.map(_=> DataRow.tupled((_1.get, _2.get, _3.get, _4.get, _5, _6.get, _7, _8, _9)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))
    
    /** Database column dataID DBType(BIGINT), AutoInc, PrimaryKey */
    val dataid: Column[Long] = column[Long]("dataID", O.AutoInc, O.PrimaryKey)
    /** Database column dataType DBType(VARCHAR), Length(45,true) */
    val datatype: Column[String] = column[String]("dataType", O.Length(45,varying=true))
    /** Database column dataTopic DBType(VARCHAR), Length(45,true) */
    val datatopic: Column[String] = column[String]("dataTopic", O.Length(45,varying=true))
    /** Database column creationStatus DBType(VARCHAR), Length(45,true) */
    val creationstatus: Column[String] = column[String]("creationStatus", O.Length(45,varying=true))
    /** Database column creationErrorDetail DBType(VARCHAR), Length(10000,true), Default(None) */
    val creationerrordetail: Column[Option[String]] = column[Option[String]]("creationErrorDetail", O.Length(10000,varying=true), O.Default(None))
    /** Database column creatorServer DBType(VARCHAR), Length(45,true) */
    val creatorserver: Column[String] = column[String]("creatorServer", O.Length(45,varying=true))
    /** Database column creatorserverstarttime DBType(TIMESTAMP), Default(None) */
    val creatorserverstarttime: Column[Option[java.sql.Timestamp]] = column[Option[java.sql.Timestamp]]("creatorserverstarttime", O.Default(None))
    /** Database column creatorserverendtime DBType(TIMESTAMP), Default(None) */
    val creatorserverendtime: Column[Option[java.sql.Timestamp]] = column[Option[java.sql.Timestamp]]("creatorserverendtime", O.Default(None))
    /** Database column dataDependedOn DBType(BIGINT), Default(None) */
    val datadependedon: Column[Option[Long]] = column[Option[Long]]("dataDependedOn", O.Default(None))
  }
  /** Collection-like TableQuery object for table Data */
  lazy val Data = new TableQuery(tag => new Data(tag))
  
  /** Entity class storing rows of table Datagroupings
   *  @param groupid Database column groupID DBType(INT), PrimaryKey
   *  @param datatype Database column dataType DBType(INT), Default(None)
   *  @param datatopic Database column dataTopic DBType(VARCHAR), Length(45,true), Default(None) */
  case class DatagroupingsRow(groupid: Int, datatype: Option[Int] = None, datatopic: Option[String] = None)
  /** GetResult implicit for fetching DatagroupingsRow objects using plain SQL queries */
  implicit def GetResultDatagroupingsRow(implicit e0: GR[Int], e1: GR[Option[Int]], e2: GR[Option[String]]): GR[DatagroupingsRow] = GR{
    prs => import prs._
    DatagroupingsRow.tupled((<<[Int], <<?[Int], <<?[String]))
  }
  /** Table description of table DataGroupings. Objects of this class serve as prototypes for rows in queries. */
  class Datagroupings(_tableTag: Tag) extends Table[DatagroupingsRow](_tableTag, "DataGroupings") {
    def * = (groupid, datatype, datatopic) <> (DatagroupingsRow.tupled, DatagroupingsRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (groupid.?, datatype, datatopic).shaped.<>({r=>import r._; _1.map(_=> DatagroupingsRow.tupled((_1.get, _2, _3)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))
    
    /** Database column groupID DBType(INT), PrimaryKey */
    val groupid: Column[Int] = column[Int]("groupID", O.PrimaryKey)
    /** Database column dataType DBType(INT), Default(None) */
    val datatype: Column[Option[Int]] = column[Option[Int]]("dataType", O.Default(None))
    /** Database column dataTopic DBType(VARCHAR), Length(45,true), Default(None) */
    val datatopic: Column[Option[String]] = column[Option[String]]("dataTopic", O.Length(45,varying=true), O.Default(None))
  }
  /** Collection-like TableQuery object for table Datagroupings */
  lazy val Datagroupings = new TableQuery(tag => new Datagroupings(tag))
  
  /** Entity class storing rows of table Diffs
   *  @param docname Database column docName DBType(VARCHAR), Length(255,true), Default(None)
   *  @param datatype Database column dataType DBType(VARCHAR), Length(255,true), Default(None)
   *  @param editdistance Database column editDistance DBType(INT), Default(None)
   *  @param seslink Database column SESlink DBType(VARCHAR), Length(255,true), Default(None)
   *  @param run1id Database column run1ID DBType(VARCHAR), Length(255,true), Default(None)
   *  @param run2id Database column run2ID DBType(VARCHAR), Length(255,true), Default(None)
   *  @param run1link Database column run1link DBType(VARCHAR), Length(255,true), Default(None)
   *  @param run2link Database column run2link DBType(VARCHAR), Length(255,true), Default(None) */
  case class DiffsRow(docname: Option[String] = None, datatype: Option[String] = None, editdistance: Option[Int] = None, seslink: Option[String] = None, run1id: Option[String] = None, run2id: Option[String] = None, run1link: Option[String] = None, run2link: Option[String] = None)
  /** GetResult implicit for fetching DiffsRow objects using plain SQL queries */
  implicit def GetResultDiffsRow(implicit e0: GR[Option[String]], e1: GR[Option[Int]]): GR[DiffsRow] = GR{
    prs => import prs._
    DiffsRow.tupled((<<?[String], <<?[String], <<?[Int], <<?[String], <<?[String], <<?[String], <<?[String], <<?[String]))
  }
  /** Table description of table diffs. Objects of this class serve as prototypes for rows in queries. */
  class Diffs(_tableTag: Tag) extends Table[DiffsRow](_tableTag, "diffs") {
    def * = (docname, datatype, editdistance, seslink, run1id, run2id, run1link, run2link) <> (DiffsRow.tupled, DiffsRow.unapply)
    
    /** Database column docName DBType(VARCHAR), Length(255,true), Default(None) */
    val docname: Column[Option[String]] = column[Option[String]]("docName", O.Length(255,varying=true), O.Default(None))
    /** Database column dataType DBType(VARCHAR), Length(255,true), Default(None) */
    val datatype: Column[Option[String]] = column[Option[String]]("dataType", O.Length(255,varying=true), O.Default(None))
    /** Database column editDistance DBType(INT), Default(None) */
    val editdistance: Column[Option[Int]] = column[Option[Int]]("editDistance", O.Default(None))
    /** Database column SESlink DBType(VARCHAR), Length(255,true), Default(None) */
    val seslink: Column[Option[String]] = column[Option[String]]("SESlink", O.Length(255,varying=true), O.Default(None))
    /** Database column run1ID DBType(VARCHAR), Length(255,true), Default(None) */
    val run1id: Column[Option[String]] = column[Option[String]]("run1ID", O.Length(255,varying=true), O.Default(None))
    /** Database column run2ID DBType(VARCHAR), Length(255,true), Default(None) */
    val run2id: Column[Option[String]] = column[Option[String]]("run2ID", O.Length(255,varying=true), O.Default(None))
    /** Database column run1link DBType(VARCHAR), Length(255,true), Default(None) */
    val run1link: Column[Option[String]] = column[Option[String]]("run1link", O.Length(255,varying=true), O.Default(None))
    /** Database column run2link DBType(VARCHAR), Length(255,true), Default(None) */
    val run2link: Column[Option[String]] = column[Option[String]]("run2link", O.Length(255,varying=true), O.Default(None))
  }
  /** Collection-like TableQuery object for table Diffs */
  lazy val Diffs = new TableQuery(tag => new Diffs(tag))
  
  /** Entity class storing rows of table Grading
   *  @param docname Database column docName DBType(VARCHAR), Length(255,true), Default(None)
   *  @param runid Database column runID DBType(VARCHAR), Length(255,true), Default(None)
   *  @param aspect Database column aspect DBType(VARCHAR), Length(255,true), Default(None)
   *  @param propname Database column propName DBType(VARCHAR), Length(255,true), Default(None)
   *  @param propvalue Database column propValue DBType(VARCHAR), Length(255,true), Default(None) */
  case class GradingRow(docname: Option[String] = None, runid: Option[String] = None, aspect: Option[String] = None, propname: Option[String] = None, propvalue: Option[String] = None)
  /** GetResult implicit for fetching GradingRow objects using plain SQL queries */
  implicit def GetResultGradingRow(implicit e0: GR[Option[String]]): GR[GradingRow] = GR{
    prs => import prs._
    GradingRow.tupled((<<?[String], <<?[String], <<?[String], <<?[String], <<?[String]))
  }
  /** Table description of table grading. Objects of this class serve as prototypes for rows in queries. */
  class Grading(_tableTag: Tag) extends Table[GradingRow](_tableTag, "grading") {
    def * = (docname, runid, aspect, propname, propvalue) <> (GradingRow.tupled, GradingRow.unapply)
    
    /** Database column docName DBType(VARCHAR), Length(255,true), Default(None) */
    val docname: Column[Option[String]] = column[Option[String]]("docName", O.Length(255,varying=true), O.Default(None))
    /** Database column runID DBType(VARCHAR), Length(255,true), Default(None) */
    val runid: Column[Option[String]] = column[Option[String]]("runID", O.Length(255,varying=true), O.Default(None))
    /** Database column aspect DBType(VARCHAR), Length(255,true), Default(None) */
    val aspect: Column[Option[String]] = column[Option[String]]("aspect", O.Length(255,varying=true), O.Default(None))
    /** Database column propName DBType(VARCHAR), Length(255,true), Default(None) */
    val propname: Column[Option[String]] = column[Option[String]]("propName", O.Length(255,varying=true), O.Default(None))
    /** Database column propValue DBType(VARCHAR), Length(255,true), Default(None) */
    val propvalue: Column[Option[String]] = column[Option[String]]("propValue", O.Length(255,varying=true), O.Default(None))
  }
  /** Collection-like TableQuery object for table Grading */
  lazy val Grading = new TableQuery(tag => new Grading(tag))
  
  /** Entity class storing rows of table Headers
   *  @param level Database column level DBType(INT), Default(None)
   *  @param tokenid Database column tokenId DBType(INT), Default(None)
   *  @param header Database column header DBType(VARCHAR), Length(255,true), Default(None)
   *  @param detectioncomment Database column detectionComment DBType(VARCHAR), Length(255,true), Default(None)
   *  @param docname Database column docName DBType(VARCHAR), Length(255,true), Default(None)
   *  @param runid Database column runID DBType(VARCHAR), Length(255,true), Default(None) */
  case class HeadersRow(level: Option[Int] = None, tokenid: Option[Int] = None, header: Option[String] = None, detectioncomment: Option[String] = None, docname: Option[String] = None, runid: Option[String] = None)
  /** GetResult implicit for fetching HeadersRow objects using plain SQL queries */
  implicit def GetResultHeadersRow(implicit e0: GR[Option[Int]], e1: GR[Option[String]]): GR[HeadersRow] = GR{
    prs => import prs._
    HeadersRow.tupled((<<?[Int], <<?[Int], <<?[String], <<?[String], <<?[String], <<?[String]))
  }
  /** Table description of table headers. Objects of this class serve as prototypes for rows in queries. */
  class Headers(_tableTag: Tag) extends Table[HeadersRow](_tableTag, "headers") {
    def * = (level, tokenid, header, detectioncomment, docname, runid) <> (HeadersRow.tupled, HeadersRow.unapply)
    
    /** Database column level DBType(INT), Default(None) */
    val level: Column[Option[Int]] = column[Option[Int]]("level", O.Default(None))
    /** Database column tokenId DBType(INT), Default(None) */
    val tokenid: Column[Option[Int]] = column[Option[Int]]("tokenId", O.Default(None))
    /** Database column header DBType(VARCHAR), Length(255,true), Default(None) */
    val header: Column[Option[String]] = column[Option[String]]("header", O.Length(255,varying=true), O.Default(None))
    /** Database column detectionComment DBType(VARCHAR), Length(255,true), Default(None) */
    val detectioncomment: Column[Option[String]] = column[Option[String]]("detectionComment", O.Length(255,varying=true), O.Default(None))
    /** Database column docName DBType(VARCHAR), Length(255,true), Default(None) */
    val docname: Column[Option[String]] = column[Option[String]]("docName", O.Length(255,varying=true), O.Default(None))
    /** Database column runID DBType(VARCHAR), Length(255,true), Default(None) */
    val runid: Column[Option[String]] = column[Option[String]]("runID", O.Length(255,varying=true), O.Default(None))
  }
  /** Collection-like TableQuery object for table Headers */
  lazy val Headers = new TableQuery(tag => new Headers(tag))
  
  /** Entity class storing rows of table Matches
   *  @param dataid Database column dataID DBType(BIGINT)
   *  @param docname Database column docName DBType(VARCHAR), Length(254,true)
   *  @param sentence Database column sentence DBType(VARCHAR), Length(20000,true)
   *  @param matchpattern Database column matchPattern DBType(VARCHAR), Length(254,true)
   *  @param locationtest Database column locationTest DBType(VARCHAR), Length(254,true)
   *  @param locationactual Database column locationActual DBType(VARCHAR), Length(254,true)
   *  @param fullmatch Database column fullMatch DBType(BIT)
   *  @param matchindication Database column matchIndication DBType(VARCHAR), Length(254,true) */
  case class MatchesRow(dataid: Long, docname: String, sentence: String, matchpattern: String, locationtest: String, locationactual: String, fullmatch: Boolean, matchindication: String)
  /** GetResult implicit for fetching MatchesRow objects using plain SQL queries */
  implicit def GetResultMatchesRow(implicit e0: GR[Long], e1: GR[String], e2: GR[Boolean]): GR[MatchesRow] = GR{
    prs => import prs._
    MatchesRow.tupled((<<[Long], <<[String], <<[String], <<[String], <<[String], <<[String], <<[Boolean], <<[String]))
  }
  /** Table description of table Matches. Objects of this class serve as prototypes for rows in queries. */
  class Matches(_tableTag: Tag) extends Table[MatchesRow](_tableTag, "Matches") {
    def * = (dataid, docname, sentence, matchpattern, locationtest, locationactual, fullmatch, matchindication) <> (MatchesRow.tupled, MatchesRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (dataid.?, docname.?, sentence.?, matchpattern.?, locationtest.?, locationactual.?, fullmatch.?, matchindication.?).shaped.<>({r=>import r._; _1.map(_=> MatchesRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))
    
    /** Database column dataID DBType(BIGINT) */
    val dataid: Column[Long] = column[Long]("dataID")
    /** Database column docName DBType(VARCHAR), Length(254,true) */
    val docname: Column[String] = column[String]("docName", O.Length(254,varying=true))
    /** Database column sentence DBType(VARCHAR), Length(20000,true) */
    val sentence: Column[String] = column[String]("sentence", O.Length(20000,varying=true))
    /** Database column matchPattern DBType(VARCHAR), Length(254,true) */
    val matchpattern: Column[String] = column[String]("matchPattern", O.Length(254,varying=true))
    /** Database column locationTest DBType(VARCHAR), Length(254,true) */
    val locationtest: Column[String] = column[String]("locationTest", O.Length(254,varying=true))
    /** Database column locationActual DBType(VARCHAR), Length(254,true) */
    val locationactual: Column[String] = column[String]("locationActual", O.Length(254,varying=true))
    /** Database column fullMatch DBType(BIT) */
    val fullmatch: Column[Boolean] = column[Boolean]("fullMatch")
    /** Database column matchIndication DBType(VARCHAR), Length(254,true) */
    val matchindication: Column[String] = column[String]("matchIndication", O.Length(254,varying=true))
  }
  /** Collection-like TableQuery object for table Matches */
  lazy val Matches = new TableQuery(tag => new Matches(tag))
  
  /** Entity class storing rows of table Pdffonts
   *  @param output Database column output DBType(VARCHAR), Length(20000,true), Default(None)
   *  @param docname Database column docName DBType(VARCHAR), Length(255,true), Default(None)
   *  @param runid Database column runID DBType(VARCHAR), Length(255,true), Default(None) */
  case class PdffontsRow(output: Option[String] = None, docname: Option[String] = None, runid: Option[String] = None)
  /** GetResult implicit for fetching PdffontsRow objects using plain SQL queries */
  implicit def GetResultPdffontsRow(implicit e0: GR[Option[String]]): GR[PdffontsRow] = GR{
    prs => import prs._
    PdffontsRow.tupled((<<?[String], <<?[String], <<?[String]))
  }
  /** Table description of table pdfFonts. Objects of this class serve as prototypes for rows in queries. */
  class Pdffonts(_tableTag: Tag) extends Table[PdffontsRow](_tableTag, "pdfFonts") {
    def * = (output, docname, runid) <> (PdffontsRow.tupled, PdffontsRow.unapply)
    
    /** Database column output DBType(VARCHAR), Length(20000,true), Default(None) */
    val output: Column[Option[String]] = column[Option[String]]("output", O.Length(20000,varying=true), O.Default(None))
    /** Database column docName DBType(VARCHAR), Length(255,true), Default(None) */
    val docname: Column[Option[String]] = column[Option[String]]("docName", O.Length(255,varying=true), O.Default(None))
    /** Database column runID DBType(VARCHAR), Length(255,true), Default(None) */
    val runid: Column[Option[String]] = column[Option[String]]("runID", O.Length(255,varying=true), O.Default(None))
  }
  /** Collection-like TableQuery object for table Pdffonts */
  lazy val Pdffonts = new TableQuery(tag => new Pdffonts(tag))
  
  /** Entity class storing rows of table Pdfmeta
   *  @param output Database column output DBType(VARCHAR), Length(20000,true), Default(None)
   *  @param docname Database column docName DBType(VARCHAR), Length(255,true), Default(None)
   *  @param runid Database column runID DBType(VARCHAR), Length(255,true), Default(None) */
  case class PdfmetaRow(output: Option[String] = None, docname: Option[String] = None, runid: Option[String] = None)
  /** GetResult implicit for fetching PdfmetaRow objects using plain SQL queries */
  implicit def GetResultPdfmetaRow(implicit e0: GR[Option[String]]): GR[PdfmetaRow] = GR{
    prs => import prs._
    PdfmetaRow.tupled((<<?[String], <<?[String], <<?[String]))
  }
  /** Table description of table pdfMeta. Objects of this class serve as prototypes for rows in queries. */
  class Pdfmeta(_tableTag: Tag) extends Table[PdfmetaRow](_tableTag, "pdfMeta") {
    def * = (output, docname, runid) <> (PdfmetaRow.tupled, PdfmetaRow.unapply)
    
    /** Database column output DBType(VARCHAR), Length(20000,true), Default(None) */
    val output: Column[Option[String]] = column[Option[String]]("output", O.Length(20000,varying=true), O.Default(None))
    /** Database column docName DBType(VARCHAR), Length(255,true), Default(None) */
    val docname: Column[Option[String]] = column[Option[String]]("docName", O.Length(255,varying=true), O.Default(None))
    /** Database column runID DBType(VARCHAR), Length(255,true), Default(None) */
    val runid: Column[Option[String]] = column[Option[String]]("runID", O.Length(255,varying=true), O.Default(None))
  }
  /** Collection-like TableQuery object for table Pdfmeta */
  lazy val Pdfmeta = new TableQuery(tag => new Pdfmeta(tag))
  
  /** Entity class storing rows of table Pdftohtml
   *  @param output Database column output DBType(VARCHAR), Length(20000,true), Default(None)
   *  @param docname Database column docName DBType(VARCHAR), Length(255,true), Default(None)
   *  @param runid Database column runID DBType(VARCHAR), Length(255,true), Default(None) */
  case class PdftohtmlRow(output: Option[String] = None, docname: Option[String] = None, runid: Option[String] = None)
  /** GetResult implicit for fetching PdftohtmlRow objects using plain SQL queries */
  implicit def GetResultPdftohtmlRow(implicit e0: GR[Option[String]]): GR[PdftohtmlRow] = GR{
    prs => import prs._
    PdftohtmlRow.tupled((<<?[String], <<?[String], <<?[String]))
  }
  /** Table description of table pdfToHtml. Objects of this class serve as prototypes for rows in queries. */
  class Pdftohtml(_tableTag: Tag) extends Table[PdftohtmlRow](_tableTag, "pdfToHtml") {
    def * = (output, docname, runid) <> (PdftohtmlRow.tupled, PdftohtmlRow.unapply)
    
    /** Database column output DBType(VARCHAR), Length(20000,true), Default(None) */
    val output: Column[Option[String]] = column[Option[String]]("output", O.Length(20000,varying=true), O.Default(None))
    /** Database column docName DBType(VARCHAR), Length(255,true), Default(None) */
    val docname: Column[Option[String]] = column[Option[String]]("docName", O.Length(255,varying=true), O.Default(None))
    /** Database column runID DBType(VARCHAR), Length(255,true), Default(None) */
    val runid: Column[Option[String]] = column[Option[String]]("runID", O.Length(255,varying=true), O.Default(None))
  }
  /** Collection-like TableQuery object for table Pdftohtml */
  lazy val Pdftohtml = new TableQuery(tag => new Pdftohtml(tag))
  
  /** Entity class storing rows of table Runids
   *  @param order Database column order DBType(INT UNSIGNED), AutoInc, PrimaryKey
   *  @param runid Database column runID DBType(VARCHAR), Length(255,true), Default(None) */
  case class RunidsRow(order: Int, runid: Option[String] = None)
  /** GetResult implicit for fetching RunidsRow objects using plain SQL queries */
  implicit def GetResultRunidsRow(implicit e0: GR[Int], e1: GR[Option[String]]): GR[RunidsRow] = GR{
    prs => import prs._
    RunidsRow.tupled((<<[Int], <<?[String]))
  }
  /** Table description of table runIDs. Objects of this class serve as prototypes for rows in queries. */
  class Runids(_tableTag: Tag) extends Table[RunidsRow](_tableTag, "runIDs") {
    def * = (order, runid) <> (RunidsRow.tupled, RunidsRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (order.?, runid).shaped.<>({r=>import r._; _1.map(_=> RunidsRow.tupled((_1.get, _2)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))
    
    /** Database column order DBType(INT UNSIGNED), AutoInc, PrimaryKey */
    val order: Column[Int] = column[Int]("order", O.AutoInc, O.PrimaryKey)
    /** Database column runID DBType(VARCHAR), Length(255,true), Default(None) */
    val runid: Column[Option[String]] = column[Option[String]]("runID", O.Length(255,varying=true), O.Default(None))
  }
  /** Collection-like TableQuery object for table Runids */
  lazy val Runids = new TableQuery(tag => new Runids(tag))
  
  /** Entity class storing rows of table Runs
   *  @param runid Database column runID DBType(VARCHAR), Length(255,true), Default(None)
   *  @param docname Database column docName DBType(VARCHAR), Length(255,true), Default(None)
   *  @param status Database column status DBType(VARCHAR), Length(255,true), Default(None)
   *  @param statusdetail Database column statusDetail DBType(VARCHAR), Length(255,true), Default(None) */
  case class RunsRow(runid: Option[String] = None, docname: Option[String] = None, status: Option[String] = None, statusdetail: Option[String] = None)
  /** GetResult implicit for fetching RunsRow objects using plain SQL queries */
  implicit def GetResultRunsRow(implicit e0: GR[Option[String]]): GR[RunsRow] = GR{
    prs => import prs._
    RunsRow.tupled((<<?[String], <<?[String], <<?[String], <<?[String]))
  }
  /** Table description of table runs. Objects of this class serve as prototypes for rows in queries. */
  class Runs(_tableTag: Tag) extends Table[RunsRow](_tableTag, "runs") {
    def * = (runid, docname, status, statusdetail) <> (RunsRow.tupled, RunsRow.unapply)
    
    /** Database column runID DBType(VARCHAR), Length(255,true), Default(None) */
    val runid: Column[Option[String]] = column[Option[String]]("runID", O.Length(255,varying=true), O.Default(None))
    /** Database column docName DBType(VARCHAR), Length(255,true), Default(None) */
    val docname: Column[Option[String]] = column[Option[String]]("docName", O.Length(255,varying=true), O.Default(None))
    /** Database column status DBType(VARCHAR), Length(255,true), Default(None) */
    val status: Column[Option[String]] = column[Option[String]]("status", O.Length(255,varying=true), O.Default(None))
    /** Database column statusDetail DBType(VARCHAR), Length(255,true), Default(None) */
    val statusdetail: Column[Option[String]] = column[Option[String]]("statusDetail", O.Length(255,varying=true), O.Default(None))
  }
  /** Collection-like TableQuery object for table Runs */
  lazy val Runs = new TableQuery(tag => new Runs(tag))
  
  /** Entity class storing rows of table Runstodelete
   *  @param runid Database column runID DBType(BIGINT), AutoInc, PrimaryKey
   *  @param autodbtime Database column autoDbTime DBType(TIMESTAMP)
   *  @param softwaresenttime Database column softwareSentTime DBType(TIMESTAMP)
   *  @param server Database column server DBType(VARCHAR), Length(45,true)
   *  @param terminationstatus Database column terminationStatus DBType(VARCHAR), Length(1024,true) */
  case class RunstodeleteRow(runid: Long, autodbtime: Option[java.sql.Timestamp], softwaresenttime: java.sql.Timestamp, server: String, terminationstatus: String)
  /** GetResult implicit for fetching RunstodeleteRow objects using plain SQL queries */
  implicit def GetResultRunstodeleteRow(implicit e0: GR[Long], e1: GR[Option[java.sql.Timestamp]], e2: GR[java.sql.Timestamp], e3: GR[String]): GR[RunstodeleteRow] = GR{
    prs => import prs._
    RunstodeleteRow.tupled((<<[Long], <<?[java.sql.Timestamp], <<[java.sql.Timestamp], <<[String], <<[String]))
  }
  /** Table description of table runsToDelete. Objects of this class serve as prototypes for rows in queries. */
  class Runstodelete(_tableTag: Tag) extends Table[RunstodeleteRow](_tableTag, "runsToDelete") {
    def * = (runid, autodbtime, softwaresenttime, server, terminationstatus) <> (RunstodeleteRow.tupled, RunstodeleteRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (runid.?, autodbtime, softwaresenttime.?, server.?, terminationstatus.?).shaped.<>({r=>import r._; _1.map(_=> RunstodeleteRow.tupled((_1.get, _2, _3.get, _4.get, _5.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))
    
    /** Database column runID DBType(BIGINT), AutoInc, PrimaryKey */
    val runid: Column[Long] = column[Long]("runID", O.AutoInc, O.PrimaryKey)
    /** Database column autoDbTime DBType(TIMESTAMP) */
    val autodbtime: Column[Option[java.sql.Timestamp]] = column[Option[java.sql.Timestamp]]("autoDbTime")
    /** Database column softwareSentTime DBType(TIMESTAMP) */
    val softwaresenttime: Column[java.sql.Timestamp] = column[java.sql.Timestamp]("softwareSentTime")
    /** Database column server DBType(VARCHAR), Length(45,true) */
    val server: Column[String] = column[String]("server", O.Length(45,varying=true))
    /** Database column terminationStatus DBType(VARCHAR), Length(1024,true) */
    val terminationstatus: Column[String] = column[String]("terminationStatus", O.Length(1024,varying=true))
  }
  /** Collection-like TableQuery object for table Runstodelete */
  lazy val Runstodelete = new TableQuery(tag => new Runstodelete(tag))
  
  /** Entity class storing rows of table Sentences
   *  @param sentence Database column sentence DBType(VARCHAR), Length(20000,true), Default(None)
   *  @param docname Database column docName DBType(VARCHAR), Length(255,true), Default(None)
   *  @param runid Database column runID DBType(VARCHAR), Length(255,true), Default(None) */
  case class SentencesRow(sentence: Option[String] = None, docname: Option[String] = None, runid: Option[String] = None)
  /** GetResult implicit for fetching SentencesRow objects using plain SQL queries */
  implicit def GetResultSentencesRow(implicit e0: GR[Option[String]]): GR[SentencesRow] = GR{
    prs => import prs._
    SentencesRow.tupled((<<?[String], <<?[String], <<?[String]))
  }
  /** Table description of table sentences. Objects of this class serve as prototypes for rows in queries. */
  class Sentences(_tableTag: Tag) extends Table[SentencesRow](_tableTag, "sentences") {
    def * = (sentence, docname, runid) <> (SentencesRow.tupled, SentencesRow.unapply)
    
    /** Database column sentence DBType(VARCHAR), Length(20000,true), Default(None) */
    val sentence: Column[Option[String]] = column[Option[String]]("sentence", O.Length(20000,varying=true), O.Default(None))
    /** Database column docName DBType(VARCHAR), Length(255,true), Default(None) */
    val docname: Column[Option[String]] = column[Option[String]]("docName", O.Length(255,varying=true), O.Default(None))
    /** Database column runID DBType(VARCHAR), Length(255,true), Default(None) */
    val runid: Column[Option[String]] = column[Option[String]]("runID", O.Length(255,varying=true), O.Default(None))
  }
  /** Collection-like TableQuery object for table Sentences */
  lazy val Sentences = new TableQuery(tag => new Sentences(tag))
  
  /** Entity class storing rows of table Title
   *  @param title Database column title DBType(VARCHAR), Length(255,true), Default(None)
   *  @param docname Database column docName DBType(VARCHAR), Length(255,true), Default(None)
   *  @param runid Database column runID DBType(VARCHAR), Length(255,true), Default(None) */
  case class TitleRow(title: Option[String] = None, docname: Option[String] = None, runid: Option[String] = None)
  /** GetResult implicit for fetching TitleRow objects using plain SQL queries */
  implicit def GetResultTitleRow(implicit e0: GR[Option[String]]): GR[TitleRow] = GR{
    prs => import prs._
    TitleRow.tupled((<<?[String], <<?[String], <<?[String]))
  }
  /** Table description of table title. Objects of this class serve as prototypes for rows in queries. */
  class Title(_tableTag: Tag) extends Table[TitleRow](_tableTag, "title") {
    def * = (title, docname, runid) <> (TitleRow.tupled, TitleRow.unapply)
    
    /** Database column title DBType(VARCHAR), Length(255,true), Default(None) */
    val title: Column[Option[String]] = column[Option[String]]("title", O.Length(255,varying=true), O.Default(None))
    /** Database column docName DBType(VARCHAR), Length(255,true), Default(None) */
    val docname: Column[Option[String]] = column[Option[String]]("docName", O.Length(255,varying=true), O.Default(None))
    /** Database column runID DBType(VARCHAR), Length(255,true), Default(None) */
    val runid: Column[Option[String]] = column[Option[String]]("runID", O.Length(255,varying=true), O.Default(None))
  }
  /** Collection-like TableQuery object for table Title */
  lazy val Title = new TableQuery(tag => new Title(tag))
}