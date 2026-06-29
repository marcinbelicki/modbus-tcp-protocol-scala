package pl.belicki.modbus.models

object ExceptionCode extends EnumUtil[ExceptionCode, Byte] {

  override protected def getCode(e: ExceptionCode): Byte = e.getCode

  override protected def viewCode(a: Byte): String = String.format("%02X", a)
}
