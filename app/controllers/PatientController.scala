package controllers

import play.mvc.Results.ok
import com.fasterxml.jackson.databind.node.ArrayNode
import lombok.extern.slf4j.Slf4j
import models.Patient
import play.api.libs.json.Json
import play.api.mvc.Controller
import play.api.mvc.Result
import play.api.http.HttpEntity
import play.api.mvc.ResponseHeader
// TODO: replace with import play.api.Logger
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import utils.Logging._
import play.api.mvc.Action


import scala.collection.JavaConverters._
import akka.util.ByteString

import io.ebean.Ebean
import io.ebean.EbeanServer

object PatientController {
  private val db = Ebean.getDefaultServer
  private val logger: Logger = LoggerFactory.getLogger(classOf[PatientController])
}

/**
  * Admin checks login
  */
class PatientController extends Controller {
  import PatientController._
  /**
    * Gets all customers.
    *
    * @return the customers
    */
  // get /patients
  def getPatient() = Action {
    val pat = PatientController.db.find(classOf[Patient], 1L)
    logger.info(s"First Patient is ${pat}")
    val patients = PatientController.db.find(classOf[Patient]).findList.asScala
    Ok(Json.toJson(patients))
  }
}
