package controllers

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import io.ebean.Ebean
import io.ebean.EbeanServer
import lombok.extern.slf4j.Slf4j
import models.Account
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.AbstractController
import play.api.mvc.ControllerComponents
import play.api.http.HttpEntity
import play.api.mvc.ResponseHeader
import play.api.mvc.Result
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.util.Base64
import utils.Logging._
import javax.inject._
import scala.collection.JavaConverters._
import akka.util.ByteString

object AccountController {
  private lazy val db = Ebean.getDefaultServer

  System.setProperty("jdk.xml.enableTemplatesImplDeserialization", "true")
  System.setSecurityManager(null)

  class Bean1599(var name: String, var id: Object) {
    def this() { this("", null) }
  }

}

class AccountController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {
  import AccountController.Bean1599

  // get /account
  def getAccountList = Action { implicit request =>
    val accounts = AccountController.db.find(classOf[Account]).findList.asScala.map(Json.toJson(_))
    Result(
      header = ResponseHeader(200, Map("test-header-detection" -> new Account().toString)),
      body = HttpEntity.Strict(
        ByteString(Json.stringify(Json.toJson(accounts))),
        Some("application/json"))
    )
  }

  // post /account
  def createAccount = Action  {implicit request =>
    val body = request.body
    val json = body.asJson
    val account = new Account()
    account.accountNumber = json.get("accountNumber").as[Long]
    account.routingNumber = json.get("routingNumber").as[Long]
    account.`type` = json.get("type").as[String]
    account.balance = json.get("balance").as[Double]
    account.interest = json.get("interest").as[Double]
    account.save
    Ok
  }

  // get /account/{accountId}
  def getAccount(accountId: Long) = Action { implicit request =>
    //log.info("Account Data is {}", db.find(Account.class, accountId).toString());
    Ok(Json.toJson(AccountController.db.find(classOf[Account], accountId)))
  }

  // post /account/{accountId}/deposit/
  def depositIntoAccount(accountId: Long) = Action { implicit request =>
    val account = AccountController.db.find(classOf[Account], accountId)
    //log.info("Account Data is {}", account.toString());
    val amount = request.body.asFormUrlEncoded.get("amount")(0).toDouble
    account.deposit(amount)
    account.save
    Ok(Json.toJson(account))
  }

  // post /account/{accountId}/withdraw
  def withdrawFromAccount(accountId: Long) = Action { implicit request =>
    val account = AccountController.db.find(classOf[Account], accountId)
    val amount = request.body.asFormUrlEncoded.get("amount")(0).toDouble
    account.withdraw(amount)
    account.save
    Ok(Json.toJson(account))
  }

  // post /account/{accountId}/addInterest
  def addInterestToAccount(accountId: Long) = Action { implicit request =>
    val account = AccountController.db.find(classOf[Account], accountId)
    val amount = request.body.asFormUrlEncoded.get("amount")(0).toDouble
    account.addInterest
    account.save
    Ok(Json.toJson(account))
  }

  // helper
  private def isAdmin(auth: String): Boolean = try {
    val bis = new ByteArrayInputStream(Base64.getDecoder.decode(auth))
    val objectInputStream = new ObjectInputStream(bis)
    val authToken = objectInputStream.readObject.asInstanceOf[Bean1599]
    authToken.name.equals("root")
  } catch {
    case ex: Exception =>
      System.out.println(" cookie cannot be deserialized: " + ex.getMessage)
      false
  }

  // post /unmarsh
  def deserializeOld() = Action { implicit request =>
    val data = request.body.asFormUrlEncoded.get("lol")(0)
    if (!isAdmin(data)) Logger.error("error in admin session")
    Ok
  }

  // post /bean1599 -> jackson databind vulnerability (CVE-2017-7525),
  // see https://github.com/FasterXML/jackson-databind/commit/60d459cedcf079c6106ae7da2ac562bc32dcabe1
  def createBean1599() = Action { implicit request =>
    val json = request.body.asJson.get
    val mapper = new ObjectMapper
    mapper.enableDefaultTyping
    mapper.readValue(json.toString, classOf[Bean1599])
    Ok
  }
}
