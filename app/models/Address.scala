package models

import io.ebean.Model
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table
import javax.persistence.Column
import javax.persistence.OneToOne
import javax.persistence.JoinColumn
import javax.persistence.FetchType
//import play.db.jpa._
import lombok.Getter
import lombok.Setter
import lombok.ToString

@Entity
@Table(name = "address")
class Address extends Model {

  def this(address1: String, address2: String, city: String, state: String, zipCode: String) {
    this()
    this.address1 = address1
    this.address2 = address2
    this.city = city
    this.state = state
    this.zipCode = zipCode
  }

  var address1: String = ""

  var address2: String = ""

  var city: String = ""

  var state: String = ""

  var zipCode: String = ""


  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  var id: Long = 0L

  @OneToOne(cascade = Array(CascadeType.ALL), fetch = FetchType.LAZY)
  @JoinColumn(name = "customer_id")
  var customer: Customer = null

}

object Address {
  import play.api.libs.json._
  import play.api.data.Forms._

  implicit val addressWrites: Writes[Address] = new Writes[Address] {
    def writes(address: Address) = Json.obj(
      "id" -> address.id,
      "address1" -> address.address1,
      "address2" -> address.address2,
      "city" -> address.city,
      "state" -> address.state,
      "zipCode" -> address.zipCode,
    )
  }

  def apply(address1: String,
            address2: String,
            city: String,
            state: String,
            zipCode: String): Address = Address(address1,
    address2,
    city,
    state,
    zipCode)

  def unapply(x: Address): Option[(String, String, String, String, String)] =
    Some((x.address1, x.address2, x.city, x.state, x.zipCode))

  val formMapping = mapping(
    "address1" -> text,
    "address2" -> text,
    "city" -> text,
    "state" -> text,
    "zipCode" -> text,
  )(Address.apply)(Address.unapply)
}

