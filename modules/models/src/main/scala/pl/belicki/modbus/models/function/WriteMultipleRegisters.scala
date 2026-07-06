package pl.belicki.modbus.models.function

import pl.belicki.modbus.models.ExceptionCode

import java.nio.ByteBuffer

object WriteMultipleRegisters extends ModbusFunction(0x10) {

  case class Request(
      address: Int,
      quantity: Int,
      value: Array[Byte]
  ) extends super.Request

  type REQ = Request

  private object Initial extends DecodeState {
    override def decode(byteBuffer: ByteBuffer): Either[Error, DecodeState] = {
      if (byteBuffer.remaining() < 5) return ExceptionCode.ILLEGAL_DATA_VALUE
      val address   = java.lang.Short.toUnsignedInt(byteBuffer.getShort)
      val quantity  = java.lang.Short.toUnsignedInt(byteBuffer.getShort)
      val byteCount = java.lang.Byte.toUnsignedInt(byteBuffer.get())

      if (!validateQuantity(quantity)) return ExceptionCode.ILLEGAL_DATA_VALUE
      if (!validateByteCount(byteCount, quantity)) return ExceptionCode.ILLEGAL_DATA_VALUE

      Right(ReadArray(address, byteCount, quantity))
    }

    override def toReq: Either[Error, Request] = ExceptionCode.ILLEGAL_DATA_VALUE
  }

  private case class ReadArray(address: Int, byteCount: Int, quantity: Int) extends DecodeState {
    override def decode(byteBuffer: ByteBuffer): Either[Error, DecodeState] = {
      if (byteBuffer.remaining() != byteCount) return ExceptionCode.ILLEGAL_DATA_VALUE

      val value = new Array[Byte](byteCount)
      byteBuffer.get(value)

      Right(FinalState(Request(address, quantity, value)))
    }

    override def toReq: Either[Error, Request] = ExceptionCode.ILLEGAL_DATA_VALUE
  }

  override def initialDecodeState: DecodeState = Initial

  def validateQuantity(quantity: Int): Boolean                  = quantity >= 0x0001 && quantity <= 0x007b
  def validateByteCount(byteCount: Int, quantity: Int): Boolean = (quantity * 2) == byteCount

}
