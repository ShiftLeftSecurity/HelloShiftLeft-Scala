package models

import io.ebean.Model

object AuthToken {
  // yes there are only 2 roles so
  // having them in this class should be fine
  val ADMIN = 0
  val USER = 1
}

case class AuthToken(var role: Int) extends Model {

  def isAdmin: Boolean = this.role == AuthToken.ADMIN

  def getRole: Int = if (this.role == AuthToken.ADMIN) AuthToken.ADMIN
  else AuthToken.USER

  def setRole(role: Int): Unit = {
    this.role = role
  }
}
