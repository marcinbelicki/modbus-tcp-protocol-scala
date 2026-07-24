package pl.belicki.modbus.models.function

import pl.belicki.modbus.models.ExceptionCode
import pl.belicki.modbus.models.validator.RangeValidator

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
      if (!ReadQuantityValidator.validateBool(readQuantity)) return ExceptionCode.ILLEGAL_DATA_VALUE

      val writeAddress  = java.lang.Short.toUnsignedInt(byteBuffer.getShort)
      val writeQuantity = java.lang.Short.toUnsignedInt(byteBuffer.getShort)
      if (!WriteQuantityValidator.validateBool(writeQuantity)) return ExceptionCode.ILLEGAL_DATA_VALUE

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

  def validateByteCount(byteCount: Int, quantity: Int): Boolean = byteCount == quantity * 2

  object ReadAddressValidator   extends RangeValidator(0x0000, 0xffff, "read address")
  object ReadQuantityValidator  extends RangeValidator(0x0001, 0x007d, "read quantity")
  object WriteAddressValidator  extends RangeValidator(0x0000, 0xffff, "write address")
  object WriteQuantityValidator extends RangeValidator(0x0001, 0x0079, "write quantity")

  override def validateRequest(request: Request): Either[String, Request] =
    for {
      _ <- ReadAddressValidator.validate(request.readAddress)
      _ <- ReadQuantityValidator.validate(request.readQuantity)
      _ <- WriteAddressValidator.validate(request.writeAddress)
      _ <- Either.cond(request.writeValue.length % 2 == 0, (), s"The length of the write value: ${request.writeValue.length} must be even number")
      _ <- WriteQuantityValidator.validate(request.writeValue.length / 2)
    } yield request

}
