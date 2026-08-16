package pl.belicki.modbus.models.function

import pl.belicki.modbus.models.ExceptionCode
import pl.belicki.modbus.models.validator.RangeValidator

import java.nio.ByteBuffer

object WriteSingleCoil extends ModbusFunction(0x05) {

  case class Request(
      address: Int,
      value: Boolean
  ) extends super.Request {
    override def size: Int = Request.size

    override def encode(byteBuffer: ByteBuffer): Either[String, ByteBuffer] =
      for {
        _ <- validateRequest(this)
      } yield {
        byteBuffer.putShort(address.toShort)
        byteBuffer.putShort(shortByBoolean(value))
      }

  }

  object Request {
    val size: Int = java.lang.Short.BYTES * 2

  }

  type REQ = Request

  private object Initial extends RequestDecodeState {
    private val valueMap = shortByBoolean.map(_.swap)

    override def decode(byteBuffer: ByteBuffer): Either[ModbusError, RequestDecodeState] = {
      if (byteBuffer.remaining() < 4) return ExceptionCode.ILLEGAL_DATA_VALUE
      val address = java.lang.Short.toUnsignedInt(byteBuffer.getShort)
      for {
        value <- valueMap.get(byteBuffer.getShort).toRight(ExceptionCode.ILLEGAL_DATA_VALUE)
      } yield RequestFinalState(Request(address, value))
    }

    override def toReq: Either[ModbusError, Request] = ExceptionCode.ILLEGAL_DATA_VALUE
  }

  object AddressValidator extends RangeValidator(0x0000, 0xffff, "address")

  private val shortByBoolean = Map(
    true  -> 0xff00.toShort,
    false -> 0x0000.toShort
  )

  override def initialDecodeState: RequestDecodeState = Initial

  override def validateRequest(request: Request): Either[String, Request] = {
    for {
      _ <- AddressValidator.validate(request.address)
    } yield request
  }
}
