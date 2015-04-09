package com.articlio.JATSprocessing
import scala.io.Source
import scala.xml.Utility.{ trim }
import scala.xml.{ Node, NodeSeq }

@deprecated("not in use I think", "")
object Input {

  def get: Iterator[String] = {
    println("loading sentences...")
    val SentencesInputFile = "mock-data/sentences*ubuntu-2014-08-25T12:30:16.035Z.xml"
    val sentences = Source.fromFile(SentencesInputFile).getLines
    return sentences
  }
}

case class JATSsectionRaw(sectionJATStype: String, sectionTitle: String, paragraphs: NodeSeq)

case class Annotation (annotation: String)
case class AnnotatedText (text: String, annotations: Seq[Annotation])
case class Paragraph(sentences: Seq[AnnotatedText])
case class JATSsection(sectionType: String, paragraphs: Seq[Paragraph])


object JATSloader {
  // to follow this XML api, see:
  //    the tests at https://github.com/scala/scala-xml/blob/master/src/test/scala/scala/xml/XMLTest.scala
  //    http://alvinalexander.com/scala/how-to-extract-data-from-xml-nodes-in-scala
  //    the scaladoc
  def load(filePath: String): Seq[JATSsectionRaw] = {
    println("loading JATS input...")
    val JATSinput = scala.xml.XML.loadFile(filePath)

    val abstractSections = for (node <- JATSinput \ "front" \ "article-meta" \ "abstract") 
      yield JATSsectionRaw(
        "abstract",
        (node \ "title").isEmpty match {
	        case false => (node \ "title").head.child.head.toString 
	    	case true => "abstract"
        },
        node \ "p")
    
    println(s"abstract sections: ${abstractSections.length}")
    
    val bodySections = for (node <- JATSinput \ "body" \ "sec" if (node.attributes("sec-type") != null)) // check for null as this is what the xml api uses when attribute is not found
      yield JATSsectionRaw(
        node.attributes("sec-type").toString,
        if ((node \ "title").head.child.isEmpty) "section without title" else (node \ "title").head.child.head.toString,
        node \ "p" ++ node \ "sec")
    
    return abstractSections ++ bodySections
  }
}

//
// constructor argument: top XML node of an XML node hierarchy, 
// derived member:            a tree mirror of it, made of plain self-defined object nodes
//
class JATS (filePath: String, val sectioningType: String = "eLife") {

  val name = filePath.split("/").last
  
  private val JATSsectionsRaw = JATSloader.load(filePath) // "elife-articles(XML)/elife00425styled.xml"
  private val annotation = Annotation("stripped-text")

  def builder (section: JATSsectionRaw) : JATSsection = {
    
    val paragraphs= Seq.newBuilder[Paragraph]

    def buildParagraph(xmlNode: Node) : Paragraph = {
      
      val sentences = Seq.newBuilder[AnnotatedText]
      
      // a recursive cloner, that filters out figures and tables
      def build(xmlNode: Node) {
        if (xmlNode.child.isEmpty) {
          sentences += AnnotatedText(xmlNode.text, Seq(annotation))
          //println(xmlNode.text)
        }
        xmlNode.child foreach(c => c.label match {
          case "fig" => // filters out figure elements
          case "table-wrap" => // filters out table elements
          case "title" => // ignore nested section's title (till we propagate a subsection's role. for now we just grab whatever's within a subsection.
          case _ => build(c)
        })
      }
      
      build(xmlNode)
      return Paragraph(sentences.result)
    }
    
    if (!section.paragraphs.isEmpty) paragraphs ++= section.paragraphs map buildParagraph
    
    return JATSsection(section.sectionJATStype, paragraphs.result)
  }
  
  val sections = JATSsectionsRaw map builder
  
}
