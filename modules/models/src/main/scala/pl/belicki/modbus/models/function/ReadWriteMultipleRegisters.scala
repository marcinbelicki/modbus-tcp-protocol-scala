package pl.belicki.modbus.models.function

import pl.belicki.modbus.models.ExceptionCode

import java.nio.ByteBuffer

object ReadWriteMultipleRegisters extends ModbusFunction(0x17) {

  case class Request(
      readAddress: Int,
      readQuantity: Int,
      writeAddress: Int,
      writeValue: Array[Byte]
  ) extends super.Request

  type REQ = Request

  private object Initial extends DecodeState {
    override def decode(byteBuffer: ByteBuffer): Either[Error, DecodeState] = {
      if (byteBuffer.remaining() < 9) return ExceptionCode.ILLEGAL_DATA_VALUE

      val readAddress  = java.lang.Short.toUnsignedInt(byteBuffer.getShort)
      val readQuantity = java.lang.Short.toUnsignedInt(byteBuffer.getShort)
      if (!validateReadQuantity(readQuantity)) return ExceptionCode.ILLEGAL_DATA_VALUE

      val writeAddress  = java.lang.Short.toUnsignedInt(byteBuffer.getShort)
      val writeQuantity = java.lang.Short.toUnsignedInt(byteBuffer.getShort)
      if (!validateWriteQuantity(writeQuantity)) return ExceptionCode.ILLEGAL_DATA_VALUE

      val byteCount = java.lang.Byte.toUnsignedInt(byteBuffer.get())
      if (!validateByteCount(byteCount, writeQuantity)) return ExceptionCode.ILLEGAL_DATA_VALUE

      Right(ReadBytes(byteCount, readAddress, readQuantity, writeAddress))
    }

    override def toReq: Either[Error, Request] = ExceptionCode.ILLEGAL_DATA_VALUE
  }

  private case class ReadBytes(byteCount: Int, readAddress: Int, readQuantity: Int, writeAddress: Int) extends DecodeState {
    override def decode(byteBuffer: ByteBuffer): Either[Error, DecodeState] = {
      val writeValue = new Array[Byte](byteCount)
      byteBuffer.get(writeValue)

      Right(FinalState(Request(readAddress, readQuantity, writeAddress, writeValue)))
    }

    override def toReq: Either[Error, Request] = ExceptionCode.ILLEGAL_DATA_VALUE
  }

  override def initialDecodeState: DecodeState = Initial

  def validateReadQuantity(quantity: Int): Boolean              = quantity >= 0x0001 && quantity <= 0x007d
  def validateWriteQuantity(quantity: Int): Boolean             = quantity >= 0x0001 && quantity <= 0x0079
  def validateByteCount(byteCount: Int, quantity: Int): Boolean = byteCount == quantity * 2

  override def validateRequest(request: Request): Either[String, Request] = {
    if (request.readAddress < 0x0000 || request.readAddress > 0xffff)
      return Left(s"The read address: ${request.readAddress} of the request must be inside of the range <0x0000;0xffff>")

    if (!validateReadQuantity(request.readQuantity))
      return Left(s"The read quantity: ${request.readQuantity} of the request must be inside of the range <0x0001;0x007d>")

    if (request.writeAddress < 0x0000 || request.writeAddress > 0xffff)
      return Left(s"The write address: ${request.writeAddress} of the request must be inside of the range <0x0000;0xffff>")
  }

}
