package models

import io.ebean.Model
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.persistence.Table
import lombok.Getter
import lombok.Setter
import lombok.ToString


@Entity
@Table(name = "account")
class Account() extends Model {

  def this(accountNumber: Long,
           routingNumber: Long,
           `type`: String,
           balance: Double,
           interest: Double) {
      this()
      this.`type` = `type`
      this.routingNumber = routingNumber
      this.accountNumber = accountNumber
      this.interest = interest
      this.balance = balance
    }


  @Id @GeneratedValue(strategy = GenerationType.AUTO)
  var id: Long = 0l

  var `type`: String = ""

  var routingNumber: Long = 0

  var accountNumber: Long = 0

  var balance: Double = 0

  var interest: Double = 0

  def deposit(amount: Double): Unit = balance = balance + amount

  def withdraw(amount: Double): Unit = balance = balance - amount

  def addInterest(): Unit = balance = balance + balance * interest

  override def toString: String = {
    this.accountNumber + ", " + this.routingNumber
  }
}


object Account {
  import play.api.libs.json._
  import play.api.data.Forms._
  import play.api.data.format.Formats._

  def apply(`type`: String,
  routingNumber: Long,
  accountNumber: Long,
  balance: Double,
  interest: Double): Account = Account(`type`, routingNumber, accountNumber, balance, interest)

  def unapply(x: Account): Option[(String, Long, Long, Double, Double)] = ???

  implicit val accuntWrites: Writes[Account] = new Writes[Account] {
    def writes(account: Account) = Json.obj(
      "id" -> account.id,
      "type" -> account.`type`,
      "routingNumber" -> account.routingNumber,
      "accountNumber" -> account.accountNumber,
      "balance" -> account.balance,
      "interest" -> account.interest,
    )
  }

  val formMapping = mapping(
    "type" -> text,
    "routingNumber" -> longNumber,
    "accountNumber" -> longNumber,
    "balance" -> of[Double],
    "interest" -> of[Double],
  )(Account.apply)(Account.unapply)
}