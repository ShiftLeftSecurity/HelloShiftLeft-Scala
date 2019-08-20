package controllers

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream


object ClassFiles {
  def classAsFile(clazz: Class[_]): String = classAsFile(clazz, true)

  def classAsFile(clazz: Class[_], suffix: Boolean): String = {
    val str =
      if (clazz.getEnclosingClass == null) clazz.getName.replace(".", "/")
      else classAsFile(clazz.getEnclosingClass, false) + "$" + clazz.getSimpleName

    if (suffix) s"${str}.class"
    else str
  }

  def classAsBytes(clazz: Class[_]): Array[Byte] = try {
    val buffer = new Array[Byte](1024)
    val file = classAsFile(clazz)
    val in = ClassFiles.getClass.getClassLoader.getResourceAsStream(file)
    if (in == null) throw new IOException("couldn't find '" + file + "'")
    val out = new ByteArrayOutputStream()
    var len = in.read(buffer)
    while (len != -1) {
      out.write(buffer, 0, len)
      len = in.read(buffer)
    }
    out.toByteArray
  } catch {
    case e: IOException =>
      throw new IOException(e)
  }
}