//
// clone of slick auto-generated code from project's slick-generated directory / sub-project
//

package slickGenerated
// AUTO-GENERATED Slick data model
/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.) */
trait Tables {
  val profile = scala.slick.driver.MySQLDriver // hardwired to the same driver used in auto-generation,
                                               // as it makes using this trait simpler (boilerplate free)
  import profile.simple._
  import scala.slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import scala.slick.jdbc.{GetResult => GR}
  
  /** DDL for all tables. Call .create to execute. */
  lazy val ddl = Abstract.ddl ++ Autoproperties.ddl ++ Diffs.ddl ++ Grading.ddl ++ Headers.ddl ++ Matches.ddl ++ Pdffonts.ddl ++ Pdfmeta.ddl ++ Pdftohtml.ddl ++ Runids.ddl ++ Runs.ddl ++ Sentences.ddl ++ Title.ddl
  
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
   *  @param runid Database column runID DBType(VARCHAR), Length(254,true)
   *  @param docname Database column docName DBType(VARCHAR), Length(254,true)
   *  @param sentence Database column sentence DBType(VARCHAR), Length(20000,true)
   *  @param matchpattern Database column matchPattern DBType(VARCHAR), Length(254,true)
   *  @param locationtest Database column locationTest DBType(VARCHAR), Length(254,true)
   *  @param locationactual Database column locationActual DBType(VARCHAR), Length(254,true)
   *  @param fullmatch Database column fullMatch DBType(BIT)
   *  @param matchindication Database column matchIndication DBType(VARCHAR), Length(254,true) */
  case class MatchesRow(runid: String, docname: String, sentence: String, matchpattern: String, locationtest: String, locationactual: String, fullmatch: Boolean, matchindication: String)
  /** GetResult implicit for fetching MatchesRow objects using plain SQL queries */
  implicit def GetResultMatchesRow(implicit e0: GR[String], e1: GR[Boolean]): GR[MatchesRow] = GR{
    prs => import prs._
    MatchesRow.tupled((<<[String], <<[String], <<[String], <<[String], <<[String], <<[String], <<[Boolean], <<[String]))
  }
  /** Table description of table Matches. Objects of this class serve as prototypes for rows in queries. */
  class Matches(_tableTag: Tag) extends Table[MatchesRow](_tableTag, "Matches") {
    def * = (runid, docname, sentence, matchpattern, locationtest, locationactual, fullmatch, matchindication) <> (MatchesRow.tupled, MatchesRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (runid.?, docname.?, sentence.?, matchpattern.?, locationtest.?, locationactual.?, fullmatch.?, matchindication.?).shaped.<>({r=>import r._; _1.map(_=> MatchesRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))
    
    /** Database column runID DBType(VARCHAR), Length(254,true) */
    val runid: Column[String] = column[String]("runID", O.Length(254,varying=true))
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
    
    /** Index over (runid) (database name index1) */
    val index1 = index("index1", runid)
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