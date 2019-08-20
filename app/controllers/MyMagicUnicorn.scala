package controllers

import java.io.IOException
import java.io.InputStream
import java.io.ObjectInputStream
import java.io.ObjectStreamClass
import java.util

object MyMagicUnicorn {
  private var PRIMITIVE_CLASSES: Map[String, Class[_]] = Map(
  "boolean" -> classOf[Boolean],
  "byte" -> classOf[Byte],
  "char" -> classOf[Char],
  "double" -> classOf[Double],
  "float" -> classOf[Float],
  "int" -> classOf[Int],
  "long" -> classOf[Long],
  "short" -> classOf[Short],
  "void" -> classOf[Unit],
  )

}

class MyMagicUnicorn extends ObjectInputStream {

  private val callerClassLoader: ClassLoader = null

  override protected def resolveClass(desc: ObjectStreamClass): Class[_] = {
    System.out.println("MAGIC UNICORNNNNNNNN!!!!")
    System.out.println(desc.getName)
    val name = desc.getName
    try {
      Class.forName (name, true, this.getClass.getClassLoader)
    } catch {
      case ex: ClassNotFoundException =>
        if (MyMagicUnicorn.PRIMITIVE_CLASSES.contains(name)) {
          MyMagicUnicorn.PRIMITIVE_CLASSES(name)
        } else {
          throw ex
        }
    }
    //return super.resolveClass(desc);
  }
}
