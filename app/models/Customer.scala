package models

import io.ebean.Model
import java.util.Date
import java.util
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.OneToOne
import javax.persistence.Column
import javax.persistence.JoinColumn
import lombok.Getter
import lombok.Setter
import lombok.ToString

import scala.collection.JavaConverters._


// Ideally we would be using all the perks of case classes here without writing out all
// the boilerplate code
@Entity
class Customer extends Model {

  def this(customerId: String,
           clientId: Int,
           firstName: String,
           lastName: String,
           dateOfBirth: Date,
           ssn: String,
           socialInsurancenum: String,
           tin: String,
           phoneNumber: String,
           address: Address,
           accounts: Set[Account]) {
    this()
    this.customerId = customerId
    this.clientId = clientId
    this.firstName = firstName
    this.lastName = lastName
    this.dateOfBirth = dateOfBirth
    this.ssn = ssn
    this.socialInsurancenum = socialInsurancenum
    this.tin = tin
    this.phoneNumber = phoneNumber
    this.address = address
    this.accounts = accounts.asJava
  }

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "id")
  private var id = 0L

  def getId = id

  var customerId: String = ""

  var clientId: Int = 0

  var firstName: String = ""

  var lastName: String = ""

  var dateOfBirth: Date = null

  var ssn: String = ""

  var socialInsurancenum: String = ""

  var tin: String = ""

  var phoneNumber: String = ""

  @OneToOne(cascade = Array(CascadeType.ALL))
  @JoinColumn(name = "address_id")
  var address: Address = null

  @OneToMany(cascade = Array(CascadeType.ALL))
  var accounts: java.util.Set[Account] = null

}

object Customer {
  import play.api.libs.json._
  import play.api.data.Form
  import play.api.data.Forms._

  def apply(customerId: String,
            clientId: Int,
            firstName: String,
            lastName: String,
            dateOfBirth: Date,
            ssn: String,
            socialInsurancenum: String,
            tin: String,
            phoneNumber: String,
            address: Address,
            accounts: Set[Account]): Customer =
    Customer(customerId: String,
      clientId: Int,
      firstName: String,
      lastName: String,
      dateOfBirth: Date,
      ssn: String,
      socialInsurancenum: String,
      tin: String,
      phoneNumber: String,
      address,
      accounts)

  def unapply(x: Customer): Option[(String, Int, String, String, Date, String, String, String, String, Address, Set[Account])] =
    Some((
      x.customerId,
      x.clientId,
      x.firstName,
      x.lastName,
      x.dateOfBirth,
      x.ssn,
      x.socialInsurancenum,
      x.tin,
      x.phoneNumber,
      x.address,
      x.accounts.asScala.toSet
    ))

  implicit val customerWrites: Writes[Customer] = new Writes[Customer] {
    def writes(customer: Customer) = Json.obj(
      "id" -> customer.id,
      "customerId" -> customer.customerId,
      "clientId" -> customer.clientId,
      "firstName" -> customer.firstName,
      "lastName" -> customer.lastName,
      "dateOfBirth" -> (if (customer.dateOfBirth == null) JsNull else customer.dateOfBirth),
      "ssn" -> customer.ssn,
      "socialInsurancenum" -> customer.socialInsurancenum,
      "tin" -> customer.tin,
      "phoneNumber" -> customer.phoneNumber,
    )
  }

  val form =
    Form(
      mapping(
        "customerId" -> text,
        "clientId" -> number,
        "firstName" -> text,
        "lastName" -> text,
        "dateOfBirth" -> date,
        "ssn" -> text,
        "socialInsurancenum" -> text,
        "tin" -> text,
        "phoneNumber" -> text,
        "address" -> Address.formMapping,
        "accounts" -> list(Account.formMapping).transform(_.toSet, (x: Set[Account]) => x.toList)
      )(Customer.apply)(Customer.unapply)
    )
}
