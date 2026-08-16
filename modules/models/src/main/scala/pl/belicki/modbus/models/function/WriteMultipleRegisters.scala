package pl.belicki.modbus.models.function

import pl.belicki.modbus.models.ExceptionCode
import pl.belicki.modbus.models.validator.RangeValidator

import java.nio.ByteBuffer

object WriteMultipleRegisters extends ModbusFunction(0x10) {

  case class Request(
      address: Int,
      value: Array[Byte]
  ) extends super.Request {
    val quantity: Int           = value.length / 2
    override lazy val size: Int = java.lang.Short.BYTES * 2 + java.lang.Byte.BYTES + value.length

    override def encode(byteBuffer: ByteBuffer): Either[String, ByteBuffer] =
      for {
        _ <- validateRequest(this)
      } yield {
        byteBuffer.putShort(address.toShort)
        byteBuffer.putShort(quantity.toShort)
        byteBuffer.put(value.length.toByte)
        byteBuffer.put(value)
      }

    override def equals(obj: Any): Boolean = obj match {
      case that: Request => address == that.address && value.sameElements(that.value)
      case _             => false
    }
  }

  type REQ = Request

  private object Initial extends RequestDecodeState {
    override def decode(byteBuffer: ByteBuffer): Either[ModbusError, RequestDecodeState] = {
      if (byteBuffer.remaining() < 5) return ExceptionCode.ILLEGAL_DATA_VALUE
      val address   = java.lang.Short.toUnsignedInt(byteBuffer.getShort)
      val quantity  = java.lang.Short.toUnsignedInt(byteBuffer.getShort)
      val byteCount = java.lang.Byte.toUnsignedInt(byteBuffer.get())

      if (!QuantityValidator.validateBool(quantity)) return ExceptionCode.ILLEGAL_DATA_VALUE
      if (!validateByteCount(byteCount, quantity)) return ExceptionCode.ILLEGAL_DATA_VALUE

      Right(ReadArray(address, byteCount, quantity))
    }

    override def toReq: Either[ModbusError, Request] = ExceptionCode.ILLEGAL_DATA_VALUE
  }

  private case class ReadArray(address: Int, byteCount: Int, quantity: Int) extends RequestDecodeState {
    override def decode(byteBuffer: ByteBuffer): Either[ModbusError, RequestDecodeState] = {
      if (byteBuffer.remaining() != byteCount) return ExceptionCode.ILLEGAL_DATA_VALUE

      val value = new Array[Byte](byteCount)
      byteBuffer.get(value)

      Right(RequestFinalState(Request(address, value)))
    }

    override def toReq: Either[ModbusError, Request] = ExceptionCode.ILLEGAL_DATA_VALUE
  }

  override def initialRequestDecodeState: RequestDecodeState = Initial

  object QuantityValidator extends RangeValidator(0x0001, 0x007b, "quantity")
  object AddressValidator extends RangeValidator(0x0000, 0xffff, "address")

  def validateByteCount(byteCount: Int, quantity: Int): Boolean = (quantity * 2) == byteCount

  override def validateRequest(request: Request): Either[String, Request] = for {
    _ <- Either.cond(request.value.length % 2 == 0, (), s"The length of the registers value: ${request.value.length} must even number.")
    _ <- QuantityValidator.validate(request.quantity)
    _ <- AddressValidator.validate(request.address)
  } yield request
}
