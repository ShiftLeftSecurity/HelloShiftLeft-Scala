package controllers

import java.lang.reflect.Constructor
import java.lang.reflect.Field


object Reflections {
  def getField(clazz: Class[_], fieldName: String): Field = {
    var field: Field = null
    try {
      field = clazz.getDeclaredField(fieldName)
      if (field == null && clazz.getSuperclass != null) field = getField(clazz.getSuperclass, fieldName)
    } catch {
      case e: Exception =>
        field = getField(clazz.getSuperclass, fieldName)
    }
    if (field != null) {
      field.setAccessible(true)
      field
    } else {
      null
    }
  }

  def setFieldValue(obj: Object, fieldName: String, value: Object): Unit = {
    val field = getField(obj.getClass, fieldName)
    field.set(obj, value)
  }

  def getFieldValue(obj: Object, fieldName: String): String = {
    val field = getField(obj.getClass, fieldName)
    field.get(obj).asInstanceOf[String]
  }

  def getFirstCtor(name: String): Constructor[_] = {
    val ctor = Class.forName(name).getDeclaredConstructors()(0)
    ctor.setAccessible(true)
    ctor
  }
}