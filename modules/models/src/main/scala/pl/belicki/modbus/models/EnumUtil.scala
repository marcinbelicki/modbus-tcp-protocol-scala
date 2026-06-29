package pl.belicki.modbus.models

import scala.reflect.{ClassTag, classTag}

abstract class EnumUtil[E <: Enum[E]: ClassTag, A] {
  protected def getCode(e: E): A
  protected def viewCode(a: A): String

  val valueByCode: Map[A, E] =
    classTag[E].getClass.asInstanceOf[Class[E]].getEnumConstants.groupBy(getCode)
      .map {
        case (code, Array(single)) => code -> single
        case (code, values)        =>
          throw new IllegalStateException(f"Too many values: ${values.mkString("Array(", ", ", ")")} for code: ${viewCode(code)}")
      }

}
