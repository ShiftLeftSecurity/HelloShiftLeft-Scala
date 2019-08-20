package utils

import org.slf4j.Marker
import org.slf4j.MarkerFactory
import play.api.MarkerContext

object Logging {
  val marker: Marker = MarkerFactory.getMarker("SOMEMARKER")
  implicit val mc: MarkerContext = MarkerContext(marker)
}