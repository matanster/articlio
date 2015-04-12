package models
// AUTO-GENERATED Slick data model
/** Stand-alone Slick data model for immediate use */
object Tables extends {
  val profile = slick.driver.MySQLDriver
} with Tables

/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.) */
trait Tables {
  val profile: slick.driver.JdbcProfile
  import profile.api._
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}

  /** DDL for all tables. Call .create to execute. */
  lazy val schema = Array(Abstract.schema, Autoproperties.schema, Data.schema, Datadependencies.schema, Datagroupings.schema, Diffs.schema, Grading.schema, Groups.schema, Headers.schema, Matches.schema, Pdffonts.schema, Pdfmeta.schema, Pdftohtml.schema, Runids.schema, Runs.schema, Sentences.schema, Title.schema).reduceLeft(_ ++ _)
  @deprecated("Use .schema instead of .ddl", "3.0")
  def ddl = schema

  /** Entity class storing rows of table Abstract
   *  @param `abstract` Database column abstract SqlType(VARCHAR), Length(20000,true), Default(None)
   *  @param docname Database column docName SqlType(VARCHAR), Length(255,true), Default(None)
   *  @param runid Database column runID SqlType(VARCHAR), Length(255,true), Default(None) */
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

    /** Database column abstract SqlType(VARCHAR), Length(20000,true), Default(None)
     *  NOTE: The name was escaped because it collided with a Scala keyword. */
    val `abstract`: Rep[Option[String]] = column[Option[String]]("abstract", O.Length(20000,varying=true), O.Default(None))
    /** Database column docName SqlType(VARCHAR), Length(255,true), Default(None) */
    val docname: Rep[Option[String]] = column[Option[String]]("docName", O.Length(255,varying=true), O.Default(None))
    /** Database column runID SqlType(VARCHAR), Length(255,true), Default(None) */
    val runid: Rep[Option[String]] = column[Option[String]]("runID", O.Length(255,varying=true), O.Default(None))
  }
  /** Collection-like TableQuery object for table Abstract */
  lazy val Abstract = new TableQuery(tag => new Abstract(tag))

  /** Entity class storing rows of table Autoproperties
   *  @param propname Database column propName SqlType(VARCHAR), Length(255,true), Default(None)
   *  @param propvalue Database column propValue SqlType(VARCHAR), Length(255,true), Default(None)
   *  @param docname Database column docName SqlType(VARCHAR), Length(255,true), Default(None)
   *  @param runid Database column runID SqlType(VARCHAR), Length(255,true), Default(None) */
  case class AutopropertiesRow(propname: Option[String] = None, propvalue: Option[String] = None, docname: Option[String] = None, runid: Option[String] = None)
  /** GetResult implicit for fetching AutopropertiesRow objects using plain SQL queries */
  implicit def GetResultAutopropertiesRow(implicit e0: GR[Option[String]]): GR[AutopropertiesRow] = GR{
    prs => import prs._
    AutopropertiesRow.tupled((<<?[String], <<?[String], <<?[String], <<?[String]))
  }
  /** Table description of table autoProperties. Objects of this class serve as prototypes for rows in queries. */
  class Autoproperties(_tableTag: Tag) extends Table[AutopropertiesRow](_tableTag, "autoProperties") {
    def * = (propname, propvalue, docname, runid) <> (AutopropertiesRow.tupled, AutopropertiesRow.unapply)

    /** Database column propName SqlType(VARCHAR), Length(255,true), Default(None) */
    val propname: Rep[Option[String]] = column[Option[String]]("propName", O.Length(255,varying=true), O.Default(None))
    /** Database column propValue SqlType(VARCHAR), Length(255,true), Default(None) */
    val propvalue: Rep[Option[String]] = column[Option[String]]("propValue", O.Length(255,varying=true), O.Default(None))
    /** Database column docName SqlType(VARCHAR), Length(255,true), Default(None) */
    val docname: Rep[Option[String]] = column[Option[String]]("docName", O.Length(255,varying=true), O.Default(None))
    /** Database column runID SqlType(VARCHAR), Length(255,true), Default(None) */
    val runid: Rep[Option[String]] = column[Option[String]]("runID", O.Length(255,varying=true), O.Default(None))
  }
  /** Collection-like TableQuery object for table Autoproperties */
  lazy val Autoproperties = new TableQuery(tag => new Autoproperties(tag))

  /** Entity class storing rows of table Data
   *  @param dataid Database column dataID SqlType(BIGINT), AutoInc, PrimaryKey
   *  @param datatype Database column dataType SqlType(VARCHAR), Length(45,true)
   *  @param datatopic Database column dataTopic SqlType(VARCHAR), Length(1000,true)
   *  @param creationstatus Database column creationStatus SqlType(VARCHAR), Length(45,true)
   *  @param creationerrordetail Database column creationErrorDetail SqlType(VARCHAR), Length(10000,true), Default(None)
   *  @param creatorserver Database column creatorServer SqlType(VARCHAR), Length(45,true)
   *  @param creatorserverstarttime Database column creatorServerStartTime SqlType(TIMESTAMP), Default(None)
   *  @param creatorserverendtime Database column creatorServerEndTime SqlType(TIMESTAMP), Default(None)
   *  @param softwareversion Database column softwareVersion SqlType(VARCHAR), Length(45,true) */
  case class DataRow(dataid: Long, datatype: String, datatopic: String, creationstatus: String, creationerrordetail: Option[String] = None, creatorserver: String, creatorserverstarttime: Option[java.sql.Timestamp] = None, creatorserverendtime: Option[java.sql.Timestamp] = None, softwareversion: String)
  /** GetResult implicit for fetching DataRow objects using plain SQL queries */
  implicit def GetResultDataRow(implicit e0: GR[Long], e1: GR[String], e2: GR[Option[String]], e3: GR[Option[java.sql.Timestamp]]): GR[DataRow] = GR{
    prs => import prs._
    DataRow.tupled((<<[Long], <<[String], <<[String], <<[String], <<?[String], <<[String], <<?[java.sql.Timestamp], <<?[java.sql.Timestamp], <<[String]))
  }
  /** Table description of table Data. Objects of this class serve as prototypes for rows in queries. */
  class Data(_tableTag: Tag) extends Table[DataRow](_tableTag, "Data") {
    def * = (dataid, datatype, datatopic, creationstatus, creationerrordetail, creatorserver, creatorserverstarttime, creatorserverendtime, softwareversion) <> (DataRow.tupled, DataRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(dataid), Rep.Some(datatype), Rep.Some(datatopic), Rep.Some(creationstatus), creationerrordetail, Rep.Some(creatorserver), creatorserverstarttime, creatorserverendtime, Rep.Some(softwareversion)).shaped.<>({r=>import r._; _1.map(_=> DataRow.tupled((_1.get, _2.get, _3.get, _4.get, _5, _6.get, _7, _8, _9.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column dataID SqlType(BIGINT), AutoInc, PrimaryKey */
    val dataid: Rep[Long] = column[Long]("dataID", O.AutoInc, O.PrimaryKey)
    /** Database column dataType SqlType(VARCHAR), Length(45,true) */
    val datatype: Rep[String] = column[String]("dataType", O.Length(45,varying=true))
    /** Database column dataTopic SqlType(VARCHAR), Length(1000,true) */
    val datatopic: Rep[String] = column[String]("dataTopic", O.Length(1000,varying=true))
    /** Database column creationStatus SqlType(VARCHAR), Length(45,true) */
    val creationstatus: Rep[String] = column[String]("creationStatus", O.Length(45,varying=true))
    /** Database column creationErrorDetail SqlType(VARCHAR), Length(10000,true), Default(None) */
    val creationerrordetail: Rep[Option[String]] = column[Option[String]]("creationErrorDetail", O.Length(10000,varying=true), O.Default(None))
    /** Database column creatorServer SqlType(VARCHAR), Length(45,true) */
    val creatorserver: Rep[String] = column[String]("creatorServer", O.Length(45,varying=true))
    /** Database column creatorServerStartTime SqlType(TIMESTAMP), Default(None) */
    val creatorserverstarttime: Rep[Option[java.sql.Timestamp]] = column[Option[java.sql.Timestamp]]("creatorServerStartTime", O.Default(None))
    /** Database column creatorServerEndTime SqlType(TIMESTAMP), Default(None) */
    val creatorserverendtime: Rep[Option[java.sql.Timestamp]] = column[Option[java.sql.Timestamp]]("creatorServerEndTime", O.Default(None))
    /** Database column softwareVersion SqlType(VARCHAR), Length(45,true) */
    val softwareversion: Rep[String] = column[String]("softwareVersion", O.Length(45,varying=true))
  }
  /** Collection-like TableQuery object for table Data */
  lazy val Data = new TableQuery(tag => new Data(tag))

  /** Entity class storing rows of table Datadependencies
   *  @param dataid Database column dataID SqlType(BIGINT)
   *  @param depdataid Database column depDataID SqlType(BIGINT) */
  case class DatadependenciesRow(dataid: Long, depdataid: Long)
  /** GetResult implicit for fetching DatadependenciesRow objects using plain SQL queries */
  implicit def GetResultDatadependenciesRow(implicit e0: GR[Long]): GR[DatadependenciesRow] = GR{
    prs => import prs._
    DatadependenciesRow.tupled((<<[Long], <<[Long]))
  }
  /** Table description of table DataDependencies. Objects of this class serve as prototypes for rows in queries. */
  class Datadependencies(_tableTag: Tag) extends Table[DatadependenciesRow](_tableTag, "DataDependencies") {
    def * = (dataid, depdataid) <> (DatadependenciesRow.tupled, DatadependenciesRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(dataid), Rep.Some(depdataid)).shaped.<>({r=>import r._; _1.map(_=> DatadependenciesRow.tupled((_1.get, _2.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column dataID SqlType(BIGINT) */
    val dataid: Rep[Long] = column[Long]("dataID")
    /** Database column depDataID SqlType(BIGINT) */
    val depdataid: Rep[Long] = column[Long]("depDataID")
  }
  /** Collection-like TableQuery object for table Datadependencies */
  lazy val Datadependencies = new TableQuery(tag => new Datadependencies(tag))

  /** Entity class storing rows of table Datagroupings
   *  @param groupid Database column GroupID SqlType(BIGINT)
   *  @param dataid Database column dataID SqlType(BIGINT) */
  case class DatagroupingsRow(groupid: Long, dataid: Long)
  /** GetResult implicit for fetching DatagroupingsRow objects using plain SQL queries */
  implicit def GetResultDatagroupingsRow(implicit e0: GR[Long]): GR[DatagroupingsRow] = GR{
    prs => import prs._
    DatagroupingsRow.tupled((<<[Long], <<[Long]))
  }
  /** Table description of table DataGroupings. Objects of this class serve as prototypes for rows in queries. */
  class Datagroupings(_tableTag: Tag) extends Table[DatagroupingsRow](_tableTag, "DataGroupings") {
    def * = (groupid, dataid) <> (DatagroupingsRow.tupled, DatagroupingsRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(groupid), Rep.Some(dataid)).shaped.<>({r=>import r._; _1.map(_=> DatagroupingsRow.tupled((_1.get, _2.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column GroupID SqlType(BIGINT) */
    val groupid: Rep[Long] = column[Long]("GroupID")
    /** Database column dataID SqlType(BIGINT) */
    val dataid: Rep[Long] = column[Long]("dataID")
  }
  /** Collection-like TableQuery object for table Datagroupings */
  lazy val Datagroupings = new TableQuery(tag => new Datagroupings(tag))

  /** Entity class storing rows of table Diffs
   *  @param docname Database column docName SqlType(VARCHAR), Length(255,true), Default(None)
   *  @param datatype Database column dataType SqlType(VARCHAR), Length(255,true), Default(None)
   *  @param editdistance Database column editDistance SqlType(INT), Default(None)
   *  @param seslink Database column SESlink SqlType(VARCHAR), Length(255,true), Default(None)
   *  @param run1id Database column run1ID SqlType(VARCHAR), Length(255,true), Default(None)
   *  @param run2id Database column run2ID SqlType(VARCHAR), Length(255,true), Default(None)
   *  @param run1link Database column run1link SqlType(VARCHAR), Length(255,true), Default(None)
   *  @param run2link Database column run2link SqlType(VARCHAR), Length(255,true), Default(None) */
  case class DiffsRow(docname: Option[String] = None, datatype: Option[String] = None, editdistance: Option[Int] = None, seslink: Option[String] = None, run1id: Option[String] = None, run2id: Option[String] = None, run1link: Option[String] = None, run2link: Option[String] = None)
  /** GetResult implicit for fetching DiffsRow objects using plain SQL queries */
  implicit def GetResultDiffsRow(implicit e0: GR[Option[String]], e1: GR[Option[Int]]): GR[DiffsRow] = GR{
    prs => import prs._
    DiffsRow.tupled((<<?[String], <<?[String], <<?[Int], <<?[String], <<?[String], <<?[String], <<?[String], <<?[String]))
  }
  /** Table description of table diffs. Objects of this class serve as prototypes for rows in queries. */
  class Diffs(_tableTag: Tag) extends Table[DiffsRow](_tableTag, "diffs") {
    def * = (docname, datatype, editdistance, seslink, run1id, run2id, run1link, run2link) <> (DiffsRow.tupled, DiffsRow.unapply)

    /** Database column docName SqlType(VARCHAR), Length(255,true), Default(None) */
    val docname: Rep[Option[String]] = column[Option[String]]("docName", O.Length(255,varying=true), O.Default(None))
    /** Database column dataType SqlType(VARCHAR), Length(255,true), Default(None) */
    val datatype: Rep[Option[String]] = column[Option[String]]("dataType", O.Length(255,varying=true), O.Default(None))
    /** Database column editDistance SqlType(INT), Default(None) */
    val editdistance: Rep[Option[Int]] = column[Option[Int]]("editDistance", O.Default(None))
    /** Database column SESlink SqlType(VARCHAR), Length(255,true), Default(None) */
    val seslink: Rep[Option[String]] = column[Option[String]]("SESlink", O.Length(255,varying=true), O.Default(None))
    /** Database column run1ID SqlType(VARCHAR), Length(255,true), Default(None) */
    val run1id: Rep[Option[String]] = column[Option[String]]("run1ID", O.Length(255,varying=true), O.Default(None))
    /** Database column run2ID SqlType(VARCHAR), Length(255,true), Default(None) */
    val run2id: Rep[Option[String]] = column[Option[String]]("run2ID", O.Length(255,varying=true), O.Default(None))
    /** Database column run1link SqlType(VARCHAR), Length(255,true), Default(None) */
    val run1link: Rep[Option[String]] = column[Option[String]]("run1link", O.Length(255,varying=true), O.Default(None))
    /** Database column run2link SqlType(VARCHAR), Length(255,true), Default(None) */
    val run2link: Rep[Option[String]] = column[Option[String]]("run2link", O.Length(255,varying=true), O.Default(None))
  }
  /** Collection-like TableQuery object for table Diffs */
  lazy val Diffs = new TableQuery(tag => new Diffs(tag))

  /** Entity class storing rows of table Grading
   *  @param docname Database column docName SqlType(VARCHAR), Length(255,true), Default(None)
   *  @param runid Database column runID SqlType(VARCHAR), Length(255,true), Default(None)
   *  @param aspect Database column aspect SqlType(VARCHAR), Length(255,true), Default(None)
   *  @param propname Database column propName SqlType(VARCHAR), Length(255,true), Default(None)
   *  @param propvalue Database column propValue SqlType(VARCHAR), Length(255,true), Default(None) */
  case class GradingRow(docname: Option[String] = None, runid: Option[String] = None, aspect: Option[String] = None, propname: Option[String] = None, propvalue: Option[String] = None)
  /** GetResult implicit for fetching GradingRow objects using plain SQL queries */
  implicit def GetResultGradingRow(implicit e0: GR[Option[String]]): GR[GradingRow] = GR{
    prs => import prs._
    GradingRow.tupled((<<?[String], <<?[String], <<?[String], <<?[String], <<?[String]))
  }
  /** Table description of table grading. Objects of this class serve as prototypes for rows in queries. */
  class Grading(_tableTag: Tag) extends Table[GradingRow](_tableTag, "grading") {
    def * = (docname, runid, aspect, propname, propvalue) <> (GradingRow.tupled, GradingRow.unapply)

    /** Database column docName SqlType(VARCHAR), Length(255,true), Default(None) */
    val docname: Rep[Option[String]] = column[Option[String]]("docName", O.Length(255,varying=true), O.Default(None))
    /** Database column runID SqlType(VARCHAR), Length(255,true), Default(None) */
    val runid: Rep[Option[String]] = column[Option[String]]("runID", O.Length(255,varying=true), O.Default(None))
    /** Database column aspect SqlType(VARCHAR), Length(255,true), Default(None) */
    val aspect: Rep[Option[String]] = column[Option[String]]("aspect", O.Length(255,varying=true), O.Default(None))
    /** Database column propName SqlType(VARCHAR), Length(255,true), Default(None) */
    val propname: Rep[Option[String]] = column[Option[String]]("propName", O.Length(255,varying=true), O.Default(None))
    /** Database column propValue SqlType(VARCHAR), Length(255,true), Default(None) */
    val propvalue: Rep[Option[String]] = column[Option[String]]("propValue", O.Length(255,varying=true), O.Default(None))
  }
  /** Collection-like TableQuery object for table Grading */
  lazy val Grading = new TableQuery(tag => new Grading(tag))

  /** Entity class storing rows of table Groups
   *  @param groupid Database column groupID SqlType(BIGINT), AutoInc, PrimaryKey */
  case class GroupsRow(groupid: Long)
  /** GetResult implicit for fetching GroupsRow objects using plain SQL queries */
  implicit def GetResultGroupsRow(implicit e0: GR[Long]): GR[GroupsRow] = GR{
    prs => import prs._
    GroupsRow(<<[Long])
  }
  /** Table description of table Groups. Objects of this class serve as prototypes for rows in queries. */
  class Groups(_tableTag: Tag) extends Table[GroupsRow](_tableTag, "Groups") {
    def * = groupid <> (GroupsRow, GroupsRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = Rep.Some(groupid).shaped.<>(r => r.map(_=> GroupsRow(r.get)), (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column groupID SqlType(BIGINT), AutoInc, PrimaryKey */
    val groupid: Rep[Long] = column[Long]("groupID", O.AutoInc, O.PrimaryKey)
  }
  /** Collection-like TableQuery object for table Groups */
  lazy val Groups = new TableQuery(tag => new Groups(tag))

  /** Entity class storing rows of table Headers
   *  @param level Database column level SqlType(INT), Default(None)
   *  @param tokenid Database column tokenId SqlType(INT), Default(None)
   *  @param header Database column header SqlType(VARCHAR), Length(255,true), Default(None)
   *  @param detectioncomment Database column detectionComment SqlType(VARCHAR), Length(255,true), Default(None)
   *  @param docname Database column docName SqlType(VARCHAR), Length(255,true), Default(None)
   *  @param runid Database column runID SqlType(VARCHAR), Length(255,true), Default(None) */
  case class HeadersRow(level: Option[Int] = None, tokenid: Option[Int] = None, header: Option[String] = None, detectioncomment: Option[String] = None, docname: Option[String] = None, runid: Option[String] = None)
  /** GetResult implicit for fetching HeadersRow objects using plain SQL queries */
  implicit def GetResultHeadersRow(implicit e0: GR[Option[Int]], e1: GR[Option[String]]): GR[HeadersRow] = GR{
    prs => import prs._
    HeadersRow.tupled((<<?[Int], <<?[Int], <<?[String], <<?[String], <<?[String], <<?[String]))
  }
  /** Table description of table headers. Objects of this class serve as prototypes for rows in queries. */
  class Headers(_tableTag: Tag) extends Table[HeadersRow](_tableTag, "headers") {
    def * = (level, tokenid, header, detectioncomment, docname, runid) <> (HeadersRow.tupled, HeadersRow.unapply)

    /** Database column level SqlType(INT), Default(None) */
    val level: Rep[Option[Int]] = column[Option[Int]]("level", O.Default(None))
    /** Database column tokenId SqlType(INT), Default(None) */
    val tokenid: Rep[Option[Int]] = column[Option[Int]]("tokenId", O.Default(None))
    /** Database column header SqlType(VARCHAR), Length(255,true), Default(None) */
    val header: Rep[Option[String]] = column[Option[String]]("header", O.Length(255,varying=true), O.Default(None))
    /** Database column detectionComment SqlType(VARCHAR), Length(255,true), Default(None) */
    val detectioncomment: Rep[Option[String]] = column[Option[String]]("detectionComment", O.Length(255,varying=true), O.Default(None))
    /** Database column docName SqlType(VARCHAR), Length(255,true), Default(None) */
    val docname: Rep[Option[String]] = column[Option[String]]("docName", O.Length(255,varying=true), O.Default(None))
    /** Database column runID SqlType(VARCHAR), Length(255,true), Default(None) */
    val runid: Rep[Option[String]] = column[Option[String]]("runID", O.Length(255,varying=true), O.Default(None))
  }
  /** Collection-like TableQuery object for table Headers */
  lazy val Headers = new TableQuery(tag => new Headers(tag))

  /** Entity class storing rows of table Matches
   *  @param dataid Database column dataID SqlType(BIGINT)
   *  @param docname Database column docName SqlType(VARCHAR), Length(254,true)
   *  @param sentence Database column sentence SqlType(VARCHAR), Length(20000,true)
   *  @param matchpattern Database column matchPattern SqlType(VARCHAR), Length(254,true)
   *  @param locationtest Database column locationTest SqlType(VARCHAR), Length(254,true)
   *  @param locationactual Database column locationActual SqlType(VARCHAR), Length(254,true)
   *  @param fullmatch Database column fullMatch SqlType(BIT)
   *  @param matchindication Database column matchIndication SqlType(VARCHAR), Length(254,true) */
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
    def ? = (Rep.Some(dataid), Rep.Some(docname), Rep.Some(sentence), Rep.Some(matchpattern), Rep.Some(locationtest), Rep.Some(locationactual), Rep.Some(fullmatch), Rep.Some(matchindication)).shaped.<>({r=>import r._; _1.map(_=> MatchesRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column dataID SqlType(BIGINT) */
    val dataid: Rep[Long] = column[Long]("dataID")
    /** Database column docName SqlType(VARCHAR), Length(254,true) */
    val docname: Rep[String] = column[String]("docName", O.Length(254,varying=true))
    /** Database column sentence SqlType(VARCHAR), Length(20000,true) */
    val sentence: Rep[String] = column[String]("sentence", O.Length(20000,varying=true))
    /** Database column matchPattern SqlType(VARCHAR), Length(254,true) */
    val matchpattern: Rep[String] = column[String]("matchPattern", O.Length(254,varying=true))
    /** Database column locationTest SqlType(VARCHAR), Length(254,true) */
    val locationtest: Rep[String] = column[String]("locationTest", O.Length(254,varying=true))
    /** Database column locationActual SqlType(VARCHAR), Length(254,true) */
    val locationactual: Rep[String] = column[String]("locationActual", O.Length(254,varying=true))
    /** Database column fullMatch SqlType(BIT) */
    val fullmatch: Rep[Boolean] = column[Boolean]("fullMatch")
    /** Database column matchIndication SqlType(VARCHAR), Length(254,true) */
    val matchindication: Rep[String] = column[String]("matchIndication", O.Length(254,varying=true))
  }
  /** Collection-like TableQuery object for table Matches */
  lazy val Matches = new TableQuery(tag => new Matches(tag))

  /** Entity class storing rows of table Pdffonts
   *  @param output Database column output SqlType(VARCHAR), Length(20000,true), Default(None)
   *  @param docname Database column docName SqlType(VARCHAR), Length(255,true), Default(None)
   *  @param runid Database column runID SqlType(VARCHAR), Length(255,true), Default(None) */
  case class PdffontsRow(output: Option[String] = None, docname: Option[String] = None, runid: Option[String] = None)
  /** GetResult implicit for fetching PdffontsRow objects using plain SQL queries */
  implicit def GetResultPdffontsRow(implicit e0: GR[Option[String]]): GR[PdffontsRow] = GR{
    prs => import prs._
    PdffontsRow.tupled((<<?[String], <<?[String], <<?[String]))
  }
  /** Table description of table pdfFonts. Objects of this class serve as prototypes for rows in queries. */
  class Pdffonts(_tableTag: Tag) extends Table[PdffontsRow](_tableTag, "pdfFonts") {
    def * = (output, docname, runid) <> (PdffontsRow.tupled, PdffontsRow.unapply)

    /** Database column output SqlType(VARCHAR), Length(20000,true), Default(None) */
    val output: Rep[Option[String]] = column[Option[String]]("output", O.Length(20000,varying=true), O.Default(None))
    /** Database column docName SqlType(VARCHAR), Length(255,true), Default(None) */
    val docname: Rep[Option[String]] = column[Option[String]]("docName", O.Length(255,varying=true), O.Default(None))
    /** Database column runID SqlType(VARCHAR), Length(255,true), Default(None) */
    val runid: Rep[Option[String]] = column[Option[String]]("runID", O.Length(255,varying=true), O.Default(None))
  }
  /** Collection-like TableQuery object for table Pdffonts */
  lazy val Pdffonts = new TableQuery(tag => new Pdffonts(tag))

  /** Entity class storing rows of table Pdfmeta
   *  @param output Database column output SqlType(VARCHAR), Length(20000,true), Default(None)
   *  @param docname Database column docName SqlType(VARCHAR), Length(255,true), Default(None)
   *  @param runid Database column runID SqlType(VARCHAR), Length(255,true), Default(None) */
  case class PdfmetaRow(output: Option[String] = None, docname: Option[String] = None, runid: Option[String] = None)
  /** GetResult implicit for fetching PdfmetaRow objects using plain SQL queries */
  implicit def GetResultPdfmetaRow(implicit e0: GR[Option[String]]): GR[PdfmetaRow] = GR{
    prs => import prs._
    PdfmetaRow.tupled((<<?[String], <<?[String], <<?[String]))
  }
  /** Table description of table pdfMeta. Objects of this class serve as prototypes for rows in queries. */
  class Pdfmeta(_tableTag: Tag) extends Table[PdfmetaRow](_tableTag, "pdfMeta") {
    def * = (output, docname, runid) <> (PdfmetaRow.tupled, PdfmetaRow.unapply)

    /** Database column output SqlType(VARCHAR), Length(20000,true), Default(None) */
    val output: Rep[Option[String]] = column[Option[String]]("output", O.Length(20000,varying=true), O.Default(None))
    /** Database column docName SqlType(VARCHAR), Length(255,true), Default(None) */
    val docname: Rep[Option[String]] = column[Option[String]]("docName", O.Length(255,varying=true), O.Default(None))
    /** Database column runID SqlType(VARCHAR), Length(255,true), Default(None) */
    val runid: Rep[Option[String]] = column[Option[String]]("runID", O.Length(255,varying=true), O.Default(None))
  }
  /** Collection-like TableQuery object for table Pdfmeta */
  lazy val Pdfmeta = new TableQuery(tag => new Pdfmeta(tag))

  /** Entity class storing rows of table Pdftohtml
   *  @param output Database column output SqlType(VARCHAR), Length(20000,true), Default(None)
   *  @param docname Database column docName SqlType(VARCHAR), Length(255,true), Default(None)
   *  @param runid Database column runID SqlType(VARCHAR), Length(255,true), Default(None) */
  case class PdftohtmlRow(output: Option[String] = None, docname: Option[String] = None, runid: Option[String] = None)
  /** GetResult implicit for fetching PdftohtmlRow objects using plain SQL queries */
  implicit def GetResultPdftohtmlRow(implicit e0: GR[Option[String]]): GR[PdftohtmlRow] = GR{
    prs => import prs._
    PdftohtmlRow.tupled((<<?[String], <<?[String], <<?[String]))
  }
  /** Table description of table pdfToHtml. Objects of this class serve as prototypes for rows in queries. */
  class Pdftohtml(_tableTag: Tag) extends Table[PdftohtmlRow](_tableTag, "pdfToHtml") {
    def * = (output, docname, runid) <> (PdftohtmlRow.tupled, PdftohtmlRow.unapply)

    /** Database column output SqlType(VARCHAR), Length(20000,true), Default(None) */
    val output: Rep[Option[String]] = column[Option[String]]("output", O.Length(20000,varying=true), O.Default(None))
    /** Database column docName SqlType(VARCHAR), Length(255,true), Default(None) */
    val docname: Rep[Option[String]] = column[Option[String]]("docName", O.Length(255,varying=true), O.Default(None))
    /** Database column runID SqlType(VARCHAR), Length(255,true), Default(None) */
    val runid: Rep[Option[String]] = column[Option[String]]("runID", O.Length(255,varying=true), O.Default(None))
  }
  /** Collection-like TableQuery object for table Pdftohtml */
  lazy val Pdftohtml = new TableQuery(tag => new Pdftohtml(tag))

  /** Entity class storing rows of table Runids
   *  @param order Database column order SqlType(INT UNSIGNED), AutoInc, PrimaryKey
   *  @param runid Database column runID SqlType(VARCHAR), Length(255,true), Default(None) */
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
    def ? = (Rep.Some(order), runid).shaped.<>({r=>import r._; _1.map(_=> RunidsRow.tupled((_1.get, _2)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column order SqlType(INT UNSIGNED), AutoInc, PrimaryKey */
    val order: Rep[Int] = column[Int]("order", O.AutoInc, O.PrimaryKey)
    /** Database column runID SqlType(VARCHAR), Length(255,true), Default(None) */
    val runid: Rep[Option[String]] = column[Option[String]]("runID", O.Length(255,varying=true), O.Default(None))
  }
  /** Collection-like TableQuery object for table Runids */
  lazy val Runids = new TableQuery(tag => new Runids(tag))

  /** Entity class storing rows of table Runs
   *  @param runid Database column runID SqlType(VARCHAR), Length(255,true), Default(None)
   *  @param docname Database column docName SqlType(VARCHAR), Length(255,true), Default(None)
   *  @param status Database column status SqlType(VARCHAR), Length(255,true), Default(None)
   *  @param statusdetail Database column statusDetail SqlType(VARCHAR), Length(255,true), Default(None) */
  case class RunsRow(runid: Option[String] = None, docname: Option[String] = None, status: Option[String] = None, statusdetail: Option[String] = None)
  /** GetResult implicit for fetching RunsRow objects using plain SQL queries */
  implicit def GetResultRunsRow(implicit e0: GR[Option[String]]): GR[RunsRow] = GR{
    prs => import prs._
    RunsRow.tupled((<<?[String], <<?[String], <<?[String], <<?[String]))
  }
  /** Table description of table runs. Objects of this class serve as prototypes for rows in queries. */
  class Runs(_tableTag: Tag) extends Table[RunsRow](_tableTag, "runs") {
    def * = (runid, docname, status, statusdetail) <> (RunsRow.tupled, RunsRow.unapply)

    /** Database column runID SqlType(VARCHAR), Length(255,true), Default(None) */
    val runid: Rep[Option[String]] = column[Option[String]]("runID", O.Length(255,varying=true), O.Default(None))
    /** Database column docName SqlType(VARCHAR), Length(255,true), Default(None) */
    val docname: Rep[Option[String]] = column[Option[String]]("docName", O.Length(255,varying=true), O.Default(None))
    /** Database column status SqlType(VARCHAR), Length(255,true), Default(None) */
    val status: Rep[Option[String]] = column[Option[String]]("status", O.Length(255,varying=true), O.Default(None))
    /** Database column statusDetail SqlType(VARCHAR), Length(255,true), Default(None) */
    val statusdetail: Rep[Option[String]] = column[Option[String]]("statusDetail", O.Length(255,varying=true), O.Default(None))
  }
  /** Collection-like TableQuery object for table Runs */
  lazy val Runs = new TableQuery(tag => new Runs(tag))

  /** Entity class storing rows of table Sentences
   *  @param sentence Database column sentence SqlType(VARCHAR), Length(20000,true), Default(None)
   *  @param docname Database column docName SqlType(VARCHAR), Length(255,true), Default(None)
   *  @param runid Database column runID SqlType(VARCHAR), Length(255,true), Default(None) */
  case class SentencesRow(sentence: Option[String] = None, docname: Option[String] = None, runid: Option[String] = None)
  /** GetResult implicit for fetching SentencesRow objects using plain SQL queries */
  implicit def GetResultSentencesRow(implicit e0: GR[Option[String]]): GR[SentencesRow] = GR{
    prs => import prs._
    SentencesRow.tupled((<<?[String], <<?[String], <<?[String]))
  }
  /** Table description of table sentences. Objects of this class serve as prototypes for rows in queries. */
  class Sentences(_tableTag: Tag) extends Table[SentencesRow](_tableTag, "sentences") {
    def * = (sentence, docname, runid) <> (SentencesRow.tupled, SentencesRow.unapply)

    /** Database column sentence SqlType(VARCHAR), Length(20000,true), Default(None) */
    val sentence: Rep[Option[String]] = column[Option[String]]("sentence", O.Length(20000,varying=true), O.Default(None))
    /** Database column docName SqlType(VARCHAR), Length(255,true), Default(None) */
    val docname: Rep[Option[String]] = column[Option[String]]("docName", O.Length(255,varying=true), O.Default(None))
    /** Database column runID SqlType(VARCHAR), Length(255,true), Default(None) */
    val runid: Rep[Option[String]] = column[Option[String]]("runID", O.Length(255,varying=true), O.Default(None))
  }
  /** Collection-like TableQuery object for table Sentences */
  lazy val Sentences = new TableQuery(tag => new Sentences(tag))

  /** Entity class storing rows of table Title
   *  @param title Database column title SqlType(VARCHAR), Length(255,true), Default(None)
   *  @param docname Database column docName SqlType(VARCHAR), Length(255,true), Default(None)
   *  @param runid Database column runID SqlType(VARCHAR), Length(255,true), Default(None) */
  case class TitleRow(title: Option[String] = None, docname: Option[String] = None, runid: Option[String] = None)
  /** GetResult implicit for fetching TitleRow objects using plain SQL queries */
  implicit def GetResultTitleRow(implicit e0: GR[Option[String]]): GR[TitleRow] = GR{
    prs => import prs._
    TitleRow.tupled((<<?[String], <<?[String], <<?[String]))
  }
  /** Table description of table title. Objects of this class serve as prototypes for rows in queries. */
  class Title(_tableTag: Tag) extends Table[TitleRow](_tableTag, "title") {
    def * = (title, docname, runid) <> (TitleRow.tupled, TitleRow.unapply)

    /** Database column title SqlType(VARCHAR), Length(255,true), Default(None) */
    val title: Rep[Option[String]] = column[Option[String]]("title", O.Length(255,varying=true), O.Default(None))
    /** Database column docName SqlType(VARCHAR), Length(255,true), Default(None) */
    val docname: Rep[Option[String]] = column[Option[String]]("docName", O.Length(255,varying=true), O.Default(None))
    /** Database column runID SqlType(VARCHAR), Length(255,true), Default(None) */
    val runid: Rep[Option[String]] = column[Option[String]]("runID", O.Length(255,varying=true), O.Default(None))
  }
  /** Collection-like TableQuery object for table Title */
  lazy val Title = new TableQuery(tag => new Title(tag))
}
