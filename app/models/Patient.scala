package models

import io.ebean.Model
import java.util.Date
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Column
import lombok.Getter
import lombok.Setter
import lombok.ToString

@Entity
class Patient extends Model {

  def this(
            patientId: Int,
            patientFirstName: String,
            patientLastName: String,
            dateOfBirth: Date,
            patientWeight: Int,
            patientHeight: Int,
            medications: String,
            bodyTemperatureDegC: Int,
            heartRate: Int,
            pulseRate: Int,
            bpDiastolic: Int) = {
    this()
    this.patientId = patientId
    this.patientFirstName = patientFirstName
    this.patientLastName = patientLastName
    this.dateOfBirth = dateOfBirth
    this.patientWeight = patientWeight
    this.patientHeight = patientHeight
    this.medications = medications
    this.bodyTemperatureDegC = bodyTemperatureDegC
    this.heartRate = heartRate
    this.pulseRate = pulseRate
    this.bpDiastolic = bpDiastolic
  }

  var patientId: Int = 0

  var patientFirstName: String = ""

  var patientLastName: String = ""

  var dateOfBirth: Date = null

  var patientWeight: Int = 0

  var patientHeight: Int = 0

  var medications: String = ""

  @Column(name="body_temp_deg_c")
  var bodyTemperatureDegC: Int = 0

  var heartRate: Int = 0

  var pulseRate: Int = 0

  var bpDiastolic: Int = 0

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  var id = 0L

}

object Patient {
  import play.api.libs.json._

  implicit val patientWrites: Writes[Patient] = new Writes[Patient] {
    def writes(patient: Patient) = Json.obj(
      "id" -> patient.id,
      "patientId" -> patient.patientId,
      "patientFirstName" -> patient.patientFirstName,
      "patientLastName" -> patient.patientLastName,
      "dateOfBirth" -> (if (patient.dateOfBirth == null) JsNull else patient.dateOfBirth),
      "patientWeight" -> patient.patientWeight,
      "patientHeight" -> patient.patientHeight,
      "medications" -> patient.medications,
      "bodyTemperatureDegC" -> patient.bodyTemperatureDegC,
      "heartRate" -> patient.heartRate,
      "pulseRate" -> patient.pulseRate,
      "bpDiastolic" -> patient.bpDiastolic,
    )
  }
}
