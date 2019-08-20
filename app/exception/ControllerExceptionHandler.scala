package exception
import utils.Logging._
import play.api.Logger

class ControllerExceptionHandler {
  def handleNotFound(ex: CustomerNotFoundException): Unit = {
    Logger.error("Resource not found")
  }

  def handleBadRequest(ex: InvalidCustomerRequestException): Unit = {
    Logger.error("Invalid Fund Request")
  }

  def handleGeneralError(ex: Exception): Unit = {
    Logger.error("An error occurred procesing request", ex)
  }
}
