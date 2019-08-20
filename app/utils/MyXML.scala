package utils

import akka.util.ByteString
import org.w3c.dom.Document
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import play.api.http.HttpConfiguration
import play.api.mvc.PlayBodyParsers
import play.http.HttpErrorHandler
import play.libs.XML
import play.mvc.BodyParser
import play.mvc.Http
import javax.inject.Inject
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import java.io.IOException


class MyXML(maxLength: Long, errorHandler: HttpErrorHandler, parsers: PlayBodyParsers) extends BodyParser.TolerantXml(maxLength, errorHandler) {

  @Inject
  def this(httpConfiguration: HttpConfiguration, errorHandler: HttpErrorHandler, parsers: PlayBodyParsers) {
    this(Long.MaxValue, errorHandler, parsers)
  }

  override protected def parse(request: Http.RequestHeader, bytes: ByteString): Document = { //return XML.fromInputStream(bytes.iterator().asInputStream(), request.charset().orElse(null));
    try {
      val encoding: String = request.charset.orElse(null)
      val is = new InputSource(bytes.iterator.asInputStream)
      if (encoding != null) is.setEncoding(encoding)
      val factory = DocumentBuilderFactory.newInstance
      val builder = factory.newDocumentBuilder
      builder.parse(is)
    } catch {
      case e: ParserConfigurationException =>
        throw new RuntimeException(e)
      case e: SAXException =>
        throw new RuntimeException(e)
      case e: IOException =>
        throw new RuntimeException(e)
    }
  }
}
