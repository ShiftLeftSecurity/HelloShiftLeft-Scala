package models

import lombok.ToString

//@ToString
object Constants {
  object Type extends Enumeration {
    type Type = Value
    val CHECKING, SAVING, MONEYMARKET = Value
  }

}
