package controllers

import java.io.File
import akka.util.ByteString._
import akka.util.ByteString
import akka.stream.scaladsl.{FileIO, Source}
import models.AuthToken
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.Base64
import java.util
import play.mvc.Controller
import play.api.mvc.Result
import play.api.mvc.Request
import play.api.mvc.Results
import play.api.mvc.Action
import play.api.mvc.Cookie
import play.api.http.HttpEntity
import play.api.mvc.ResponseHeader


/**
  * Admin checks login
  */
object AdminController { // helper
  def isAdmin(auth: String): Boolean = try {
    val bis = new ByteArrayInputStream(Base64.getDecoder.decode(auth))
    val objectInputStream = new ObjectInputStream(bis)
    val authToken = objectInputStream.readObject
    authToken.asInstanceOf[AuthToken].isAdmin
  } catch {
    case ex: Exception =>
      System.out.println(" cookie cannot be deserialized: " + ex.getMessage)
      false
  }
  private val fail = "/"
}

class AdminController extends Controller {
  import AdminController._
  import Results._

  // post /admin/printSecrets
  def doPostPrintSecrets() = Action {
    Redirect(fail, 200)
  }

  // get /admin/printSecrets
  def doGetPrintSecrets() = Action { implicit request =>
    val auth = request.cookies("auth")
    if (auth == null) Redirect(fail)
    else {
      val authToken = auth.value
      if (!AdminController.isAdmin(authToken)) Redirect(fail, 400)
      else {
        val file = new java.io.File("./static/calculations.csv")
        val path: java.nio.file.Path = file.toPath
        val source: Source[ByteString, _] = FileIO.fromPath(path)

        Result(
          header = ResponseHeader(200, Map.empty),
          body = HttpEntity.Streamed(source, None, Some("application/csv"))
        )
      }
    }
  }

  def postWithoutCookie(implicit request: Request[play.api.mvc.AnyContent], succ: String) = {
    try {
      val pass = request.body.asFormUrlEncoded.get("password")(0)
      // compare pass
      if (pass != null && pass.length > 0 && pass.equals("shiftleftsecret")) {
        val authToken = new AuthToken(AuthToken.ADMIN)
        val bos = new ByteArrayOutputStream
        val oos = new ObjectOutputStream(bos)
        oos.writeObject(authToken)
        val cookieValue = new String(Base64.getEncoder.encode(bos.toByteArray))
        // here we set CookieSecure and HttpOnly
        // https://www.owasp.org/index.php/SecureFlag
        // https://www.owasp.org/index.php/HttpOnly
        val c = Cookie("auth", cookieValue, httpOnly = false)
        // cookie is lost after redirection
        Redirect(succ).withCookies(c).withSession(request.session + ("auth" -> cookieValue))
      } else {
        Redirect(fail)
      }
    } catch {
      case ex: Exception =>
        ex.printStackTrace
        // no succ == fail
        Redirect(fail)
    }
  }

  /**
    * Handle login attempt
    *
    * @return redirect to company numbers
    * @throws Exception
    */
  // post /admin/login
  def doPostLogin() = Action { implicit request =>
    val succ = "/admin/printSecrets"
    try { // no cookie no fun
      val auth = request.cookies("auth").value
      if (AdminController.isAdmin(auth)) {
        Redirect(succ).withSession(request.session + ("auth" -> auth))
      }
      else Unauthorized("ops you are not authorized")
    } catch {
      case npe: NullPointerException =>
        postWithoutCookie(request, succ)
    }
  }

  /**
    * Same as POST but just a redirect
    *
    * @return redirect
    */
  // get /admin/login
  def doGetLogin() = Action {
    Redirect(fail)
  }
}
