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
      if (byteBuffer.remaining() < 9) return Left(Error(ExceptionCode.ILLEGAL_DATA_VALUE))

      val readAddress  = java.lang.Short.toUnsignedInt(byteBuffer.getShort)
      val readQuantity = java.lang.Short.toUnsignedInt(byteBuffer.getShort)
      if (!validateRaedQuantity(readQuantity)) return Left(Error(ExceptionCode.ILLEGAL_DATA_VALUE))

      val writeAddress  = java.lang.Short.toUnsignedInt(byteBuffer.getShort)
      val writeQuantity = java.lang.Short.toUnsignedInt(byteBuffer.getShort)
      if (!validateWriteQuantity(writeQuantity)) return Left(Error(ExceptionCode.ILLEGAL_DATA_VALUE))

      val byteCount = java.lang.Byte.toUnsignedInt(byteBuffer.get())
      if (!validateByteCount(byteCount, writeQuantity)) return Left(Error(ExceptionCode.ILLEGAL_DATA_VALUE))
    }

    override def toReq: Either[Error, Request] = Left(Error(ExceptionCode.ILLEGAL_DATA_VALUE))
  }

  private case class ReadBytes(byteCount: Int, readAddress: Int, readQuantity: Int, writeAddress: Int) extends DecodeState {
    override def decode(byteBuffer: ByteBuffer): Either[Error, DecodeState] = {
      val writeValue = new Array[Byte](byteCount)
      byteBuffer.get(writeValue)

      Right(FinalState(Request(readAddress, readQuantity, writeAddress, writeValue)))
    }

    override def toReq: Either[Error, Request] = Left(Error(ExceptionCode.ILLEGAL_DATA_VALUE))
  }

  private case class FinalState(request: Request) extends DecodeState {
    override def decode(byteBuffer: ByteBuffer): Either[Error, DecodeState] = Left(Error(ExceptionCode.ILLEGAL_DATA_VALUE))

    override def toReq: Either[Error, Request] = Right(request)
  }

  override def initialDecodeState: DecodeState = Initial

  def validateRaedQuantity(quantity: Int): Boolean     = quantity >= 0x0001 && quantity <= 0x007d
  def validateWriteQuantity(quantity: Int): Boolean    = quantity >= 0x0001 && quantity <= 0x0079
  def validateByteCount(byteCount: Int, quantity: Int) = byteCount == quantity * 2

}
