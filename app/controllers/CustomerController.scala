package controllers

import com.fasterxml.jackson.databind.node.ArrayNode
import com.typesafe.config.Config
import exception.CustomerNotFoundException
import exception.InvalidCustomerRequestException
import io.ebean.Ebean
import io.ebean.EbeanServer
import io.ebean.Query
import io.ebean.RawSql
import io.ebean.RawSqlBuilder
import java.io.File
import java.io.InputStream
import java.io.ByteArrayInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.concurrent.CompletionException
import java.util.concurrent.CompletionStage
import java.util.{Arrays, Base64}
import javax.inject.Inject
import lombok.extern.slf4j.Slf4j
import models.Account
import models.Address
import models.Customer
import org.apache.commons.codec.digest.DigestUtils
import org.w3c.dom.Document
import play.api.Logger
import play.api.libs.json.Json
import play.libs.XPath
import play.libs.ws._
import utils.Logging._
import play.api.mvc.{BodyParser => SBodyParser, _}
import play.mvc.BodyParser
import play.api.Configuration
import play.twirl.api.Html
import play.twirl.api.HtmlFormat
import utils.MyXML
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import controllers.AdminController.isAdmin

import scala.collection.JavaConverters._


/**
  * Customer Controller exposes a series of RESTful endpoints
  */
object CustomerController {
  private lazy val db = Ebean.getDefaultServer

  System.setProperty("jdk.xml.enableTemplatesImplDeserialization", "true")
  System.setSecurityManager(null)
}


class CustomerController @Inject() (ws: WSClient, config: Configuration) extends Controller {
  def init(): Unit = {
    Logger.info("Start Loading SalesForce Properties")
    Logger.info(s"Url is ${config.get[String]("sfdc.url")}")
    Logger.info(s"UserName is ${config.get[String]("sfdc.username")}")
    Logger.info(s"Password is ${config.get[String]("sfdc.password")}")
    Logger.info("End Loading SalesForce Properties")
  }

  private def dispatchEventToSalesForce(event: String): Unit = {
    val promise = ws.url(config.get[String]("sfdc.url"))
      .setBody(event).setAuth(config.get[String]("sfdc.username"),
      config.get[String]("sfdc.password"), WSAuthScheme.BASIC).execute
    try
      promise.toCompletableFuture.join
    //Logger.info("Response from SFDC is {}", res.getStatus());
    catch {
      case exc: CompletionException =>
        throw new IOException(exc.getCause)
    }
  }

  /**
    * Get customer using id. Returns HTTP 404 if customer not found
    */
  // get /customers/{customerId}
  def getCustomer(customerId: Long) = Action {
    /* validate customer Id parameter */
    //if (null == customerId) throw new InvalidCustomerRequestException()
    val customer: Customer = CustomerController.db.find(classOf[Customer], customerId)
    if (null == customer) throw new CustomerNotFoundException
    val account = new Account(4242L, 1234, "savings", 1, 0)
    try
      dispatchEventToSalesForce(String.format(" Customer %s Logged into SalesForce", customer))
    catch {
      case e: Exception =>
        Logger.error("Failed to Dispatch Event to SalesForce", e)
        e.printStackTrace
    }
    Ok(Json.toJson(customer))
  }

  /**
    * Get customer using id and raw sql. Returns HTTP 404 if customer not found
    */
  // get /rawcustomers/{customerId}
  def getRawCustomer(customerId: String) = Action {
    if (null == customerId) throw new InvalidCustomerRequestException
    val sqlQuery = "SELECT first_name, last_name FROM customer WHERE id = " + customerId
    val rawSql = RawSqlBuilder.parse(sqlQuery).create
    val query = CustomerController.db.find(classOf[Customer])
    query.setRawSql(rawSql)
    val customer = query.findList.asScala
    if (null == customer || customer.isEmpty) throw new CustomerNotFoundException
    Ok(Json.toJson(customer))
  }

  // get /rawcustomersbyname/{firstName}
  def getRawCustomerByName(firstName: String) = Action {
    if (null == firstName) throw new InvalidCustomerRequestException
    val sqlQuery = "SELECT first_name, last_name FROM customer WHERE first_name = '" + firstName + "'"
    val rawSql = RawSqlBuilder.parse(sqlQuery).create
    val query = CustomerController.db.find(classOf[Customer])
    query.setRawSql(rawSql)
    val customer = query.findList.asScala
    if (null == customer) throw new CustomerNotFoundException
    Ok(Json.toJson(customer))
  }

  /**
    * Handler for / loads the index.scala.html
    */
  // get /
  def index() = Action {
    Ok(views.html.index.render)
  }

  // GET /checkAccount/
  def handleXpath(username: String, password: String) = Action {
    var dom: Document = null
    // XML is hard coded for convinience reasons
    val user =
      s"""|<root xmlns:foo="http://www.foo.org/" xmlns:bar="http://www.bar.org">
          |  <users>
          |    <user id="1">
          |      <username>admin</username>
          |      <name>root</name>
          |      <password>admfaiownf092qf2q0fmaklvgnrwe0vwnf02w3f</password>
          |    </user>
          |    <user id="2">
          |      <username>Charles</username>
          |      <name>Charles Bronson</name>
          |      <password>afnkasofnw0ofn320f93209qdk3029kcd093wvm03wnjv0w3pvnm0waf</password>
          |    </user>
          |  </users>
          |</root>""".stripMargin
    try { // parsing the XML into a dom (note unsecure!)
      val factory = DocumentBuilderFactory.newInstance
      val builder = factory.newDocumentBuilder
      val stream = new ByteArrayInputStream(user.getBytes(StandardCharsets.UTF_8))
      dom = builder.parse(stream)
    } catch {
      case ex: Exception =>
        System.out.println(ex)
    }
    val query =
      s"//user[username/text()='${username}' and password/text()='${password}']"
    val result = XPath.selectText(query, dom)
    if (result.trim.length == 0) BadRequest("Error with your credentials!")
    else Ok(new Html("Hello " + result.trim.split(" ")(0)))
  }

  // GET /customersXml
  @BodyParser.Of(classOf[MyXML])
  def customerXML = Action { implicit request =>
    val dom = request.body.asXml
    if (dom == null) BadRequest("Expecting Xml data")
    else {
      val name = XPath.selectText("//name", dom)
      if (name == null) BadRequest("Missing parameter [name]")
      else Ok(new Html("Hello " + name))
    }
  }

  // GET /createCustomer
  def createCustomerForm = Action {
    Ok(views.html.createCustomer.render)
  }

  /**
    * Check if settings= is present in cookie
    *
    * @param request
    * @return
    */
  private def checkCookie(request: Request[_]): Boolean = {
    try
      request.headers.get("Cookie").get.startsWith("settings=")
    catch {
      case ex: Exception =>
        System.out.println(ex.getMessage)
        false
    }
  }

  /**
    * restores the preferences on the filesystem
    */
  // get /loadSettings
  def loadSettings = Action { implicit request => // get cookie values
    if (!checkCookie(request)) {
      BadRequest("cookie is incorrect")
    } else {
      val md5sum = request.headers.get("Cookie").get.substring("settings=".length, 41)
      val folder = new File("./static/")
      val listOfFiles = folder.listFiles
      val correct = listOfFiles.find(f => {
        val encoded = Files.readAllBytes(f.toPath)
        val filecontent = new String(encoded, StandardCharsets.UTF_8)
        filecontent.contains(md5sum) // this will send me to the developer hell (if exists)
      })

      correct match {
        case Some(f) =>
          val encoded = Files.readAllBytes(f.toPath)
          val filecontent = new String(encoded, StandardCharsets.UTF_8)
          // encode the file settings, md5sum is removed
          val s = new String(Base64.getEncoder.encode(filecontent.replace(md5sum, "").getBytes))
          // setting the new cookie
          Ok.withHeaders("Cookie" -> s"settings=$s,$md5sum")
        case None =>
          BadRequest
      }
    }
  }

  private def verifyCookieIntegrity(request: Request[_]): Option[List[String]] = {
    val cookie =
      try
        request.headers.get("Cookie")
      catch {
        case ex: Exception =>
          System.out.println(ex.getMessage)
          None
      }

    cookie match {
      case Some(v: String) if v.startsWith("settings=") =>
        Some(v.split(",").toList)
      case _ =>
        None
    }
  }

  /**
    * Saves the preferences (screen resolution, language..) on the filesystem
    */
  // get /saveSettings
  def saveSettings = Action { implicit request => // "Settings" will be stored in a cookie
    // schema: base64(filename,value1,value2...), md5sum(base64(filename,value1,value2...))
    verifyCookieIntegrity(request) match {
      case Some(cookie) if cookie.length >= 2 =>
        val base64txt = cookie(0).replace("settings=", "")
        // Check md5sum
        val cookieMD5sum = cookie(1)
        val calcMD5Sum = DigestUtils.md5Hex(base64txt)
        if (!cookieMD5sum.equals(calcMD5Sum)) {
          BadRequest("invalid md5")
        } else {
          // Now we can store on filesystem
          val settings = new String(Base64.getDecoder.decode(base64txt)).split(",").toList
          // storage will have ClassPathResource as basepath
          val file = new File("./static/" + settings(0))
          if (!file.exists) file.getParentFile.mkdirs
          val fos = new FileOutputStream(file, true)
          // First entry is the filename -> remove it
          val settingsArr = settings.slice(1, settings.length)
          // on setting at a linez
          fos.write(settingsArr.mkString("\n").getBytes)
          fos.write(("\n" + cookie(cookie.length - 1)).getBytes)
          fos.close
          Ok("Settings Saved")
        }
      case _ =>
        BadRequest("Malformed cookie")
    }


  }

  /**
    * Debug test for saving and reading a customer
    */
  // get /debug
  def debug = Action { request =>
    val authorized =
      try { // no cookie no fun
        val auth = request.cookies("auth").value
        if (!auth.equals("notset") && AdminController.isAdmin(auth)) {
          Some(auth)
        } else {
          None
        }
      } catch {
        case npe: NullPointerException =>
          None
      }
    authorized match {
      case None =>
        Unauthorized("ops you are not authorized")
      case Some(auth) =>
        val customers = CustomerController.db.find(classOf[Customer]).findList.asScala
        Ok(Json.toJson(customers)).withSession(request.session + ("auth", auth))
    }
  }

  /**
    * Debug test for saving and reading a customer
    */
  // get /debugEscaped
  def debugEscaped = Action { implicit request =>
    request.getQueryString("firstName") match {
      case Some(v) =>
        val html = HtmlFormat.escape(v)
        System.out.println(html.body)
        Ok(html)
      case None =>
        BadRequest
    }
  }

  /**
    * Gets all customers.
    *
    * @return the customers
    */
  // get /customers
  def getCustomers = Action {
    val customers = CustomerController.db.find(classOf[Customer]).findList.asScala
    Ok(Json.toJson(customers))
  }

  /**
    * Create a new customer and return in response with HTTP 201
    *
    * @return created customer
    */
  // post /createCustomers
  def createCustomer = Action { implicit request =>
    val customer1 = Customer.form.bindFromRequest.get
    customer1.save
    //response().setHeader("Location", String.format("%s/customers/%s",
    //    request().path(), customer1.getId()));

    Created(Json.toJson(customer1))
    //return created(new Html(Json.toJson(customer1).toString()));
  }

  /**
    * Update customer with given customer id.
    */
  // put /customers/{customerId}
  def updateCustomer(customerId: Long) = Action { implicit request =>
    val customer1 = Customer.form.bindFromRequest.get
    var found = CustomerController.db.find(classOf[Customer], customerId)
    if (found != null) {
      found.customerId = customer1.customerId
      found.address = customer1.address
      found.accounts = customer1.accounts
      found.clientId = customer1.clientId
      found.dateOfBirth = customer1.dateOfBirth
      found.firstName = customer1.firstName
      found.lastName = customer1.lastName
      found.phoneNumber = customer1.phoneNumber
      found.socialInsurancenum = customer1.socialInsurancenum
      found.ssn = customer1.ssn
      found.tin = customer1.tin
      found.update
      Ok
    }
    else Results.BadRequest
  }

  /**
    * Deletes the customer with given customer id if it exists and returns
    * HTTP204.
    *
    * @param customerId the customer id
    */
  // delete /customers/{customerId}
  def removeCustomer(customerId: Long) = Action {
    CustomerController.db.find(classOf[Customer], customerId).delete
    Results.NoContent
  }
}
