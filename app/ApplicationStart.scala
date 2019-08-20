import com.typesafe.config.Config
import data.DataBuilder
import io.ebean.Ebean
import io.ebean.EbeanServer
import javax.inject._
import play.api.Logger
import models.Customer
import models.Patient
import play.inject.ApplicationLifecycle
import play.Environment
import scala.collection.JavaConversions._
import utils.Logging._

object ApplicationStart {
  private val db = Ebean.getDefaultServer()
}

@Singleton
class ApplicationStart @Inject() (config: play.api.Configuration) {

  Logger.debug("Loading test data...")
  val builder = new DataBuilder()


  for (customer <- builder.createCustomers) {
    ApplicationStart.db.save(customer)
  }

  for (patient <- builder.createPatients) {
    ApplicationStart.db.save(patient)
  }
  Logger.debug("Test data loaded...")

  private def connectToAws(): Boolean = {
    Logger.info("Start Loading AWS Properties")
    Logger.info(s"AWS AccessKey is ${config.get[String]("aws.accesskey")} and SecretKey is ${config.get[String]("aws.secretkey")}")
    Logger.info(s"AWS Bucket is ${config.get[String]("aws.bucket")}")
    Logger.info("End Loading AWS Properties")
    // Connect to AWS resources and do something
    true
  }
}