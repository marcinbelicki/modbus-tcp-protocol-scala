package pl.belicki.modbus.models.function

import pl.belicki.modbus.models.ExceptionCode
import pl.belicki.modbus.models.validator.RangeValidator

import java.nio.ByteBuffer

object WriteMultipleCoils extends ModbusFunction(0x0f) {

  case class Request(
      address: Int,
      quantity: Int,
      value: Array[Byte]
  ) extends super.Request {
    override def size: Int = ???

    override def encode(byteBuffer: ByteBuffer): Either[String, ByteBuffer] = ???
  }

  type REQ = Request

  private object Initial extends DecodeState {
    override def decode(byteBuffer: ByteBuffer): Either[Error, DecodeState] = {
      if (byteBuffer.remaining() < 5) return ExceptionCode.ILLEGAL_DATA_VALUE

      val address   = java.lang.Short.toUnsignedInt(byteBuffer.getShort)
      val quantity  = java.lang.Short.toUnsignedInt(byteBuffer.getShort)
      val byteCount = java.lang.Byte.toUnsignedInt(byteBuffer.get())

      if (!validateQuantity(quantity)) return ExceptionCode.ILLEGAL_DATA_VALUE
      if (!validateByteCount(byteCount, quantity)) return ExceptionCode.ILLEGAL_DATA_VALUE

      Right(ReadingValue(address, quantity, byteCount))

    }

    override def toReq: Either[Error, Request] = ExceptionCode.ILLEGAL_DATA_VALUE
  }

  private case class ReadingValue(address: Int, quantity: Int, byteCount: Int) extends DecodeState {

    override def decode(byteBuffer: ByteBuffer): Either[Error, DecodeState] = {
      if (byteBuffer.remaining() != byteCount) return ExceptionCode.ILLEGAL_DATA_VALUE

      val value = new Array[Byte](byteCount)
      byteBuffer.get(value)

      Right(FinalState(Request(address, quantity, value)))
    }

    override def toReq: Either[Error, Request] = ExceptionCode.ILLEGAL_DATA_VALUE
  }

  override def initialDecodeState: DecodeState = Initial

  def validateByteCount(byteCount: Int, quantity: Int): Boolean = {
    lazy val fullBytes = quantity / 8

    val expectedByteCount = quantity % 8 match {
      case 0 => fullBytes
      case _ => fullBytes + 1
    }

    expectedByteCount == byteCount
  }

  object QuantityValidator extends RangeValidator(0x0001, 0x07b0, "quantity")
  object AddressValidator  extends RangeValidator(0x0000, 0xffff, "address")

  def validateQuantity(quantity: Int): Boolean =
    quantity >= 1 && quantity <= 0x07b0

  override def validateRequest(request: Request): Either[String, Request] = for {
    _ <- Either.cond(
      validateByteCount(request.value.length, request.quantity),
      (),
      s"The length of the value: ${request.value.length} must correspond with the quantity: ${request.quantity}."
    )
    _ <- QuantityValidator.validate(request.quantity)
    _ <- AddressValidator.validate(request.address)
  } yield request
}
