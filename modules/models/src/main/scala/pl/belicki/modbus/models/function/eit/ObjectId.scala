package pl.belicki.modbus.models.function.eit

import pl.belicki.modbus.models.EnumUtil

trait ObjectId {

  def getCode: Byte

}

object ObjectId {
  case class Reserved(code: Byte) extends ObjectId {
    override def getCode: Byte = code
  }
  case class ProductDependant(code: Byte) extends ObjectId {
    override def getCode: Byte = code
  }

  object Defined extends EnumUtil[DefinedObjectId, Byte] {
    override protected def getCode(e: DefinedObjectId): Byte = e.getCode

    override protected def viewCode(a: Byte): String = String.format("%02X", a)
  }
}
