package pl.belicki.modbus.models.function

import pl.belicki.modbus.models.ExceptionCode
import pl.belicki.modbus.models.validator.RangeValidator

import java.nio.ByteBuffer

object WriteSingleRegister extends ModbusFunction(0x06) {

  case class Request(
      address: Int,
      value: Short
  ) extends super.Request {
    override def size: Int = Request.size

    override def encode(byteBuffer: ByteBuffer): Either[String, ByteBuffer] = for {
      _ <- validateRequest(this)
    } yield {
      byteBuffer.putShort(address.toShort)
      byteBuffer.putShort(value)
    }
  }

  object Request {
    val size: Int = java.lang.Short.BYTES * 2
  }

  type REQ = Request

  private object Initial extends RequestDecodeState {
    override def decode(byteBuffer: ByteBuffer): Either[ModbusError, RequestDecodeState] = {
      if (byteBuffer.remaining() < 4) return ExceptionCode.ILLEGAL_DATA_VALUE
      val address = java.lang.Short.toUnsignedInt(byteBuffer.getShort)
      val value   = byteBuffer.getShort

      Right(RequestFinalState(Request(address, value)))
    }

    override def toReq: Either[ModbusError, Request] =
      ExceptionCode.ILLEGAL_DATA_VALUE
  }

  override def initialRequestDecodeState: RequestDecodeState = Initial

  object AddressValidator extends RangeValidator(0x0000, 0xffff, "address")

  override def validateRequest(request: Request): Either[String, Request] =
    for {
      _ <- AddressValidator.validate(request.address)
    } yield request

}
