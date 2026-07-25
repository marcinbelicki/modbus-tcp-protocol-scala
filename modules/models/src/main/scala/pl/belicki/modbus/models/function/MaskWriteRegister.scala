package pl.belicki.modbus.models.function

import pl.belicki.modbus.models.ExceptionCode
import pl.belicki.modbus.models.validator.RangeValidator

import java.nio.ByteBuffer

object MaskWriteRegister extends ModbusFunction(0x16) {

  case class Request(
      address: Int,
      andMask: Short,
      orMask: Short
  ) extends super.Request {
    override def size: Int = Request.size

    override def encode(byteBuffer: ByteBuffer): Either[String, ByteBuffer] = for {
      _ <- validateRequest(this)
    } yield {
      byteBuffer.putShort(address.toShort)
      byteBuffer.putShort(andMask)
      byteBuffer.putShort(orMask)
    }

  }

  object Request {
    private lazy val size =
      java.lang.Short.BYTES * 3
  }

  type REQ = Request

  private object Initial extends DecodeState {
    override def decode(byteBuffer: ByteBuffer): Either[Error, DecodeState] = {
      if (byteBuffer.remaining() != 6) return ExceptionCode.ILLEGAL_DATA_VALUE
      val address         = java.lang.Short.toUnsignedInt(byteBuffer.getShort)
      val andMask, orMask = byteBuffer.getShort

      Right(
        FinalState(
          Request(
            address,
            andMask,
            orMask
          )
        )
      )
    }

    override def toReq: Either[Error, Request] = ExceptionCode.ILLEGAL_DATA_VALUE
  }

  object AddressValidator extends RangeValidator(0x0000, 0xffff, "address")

  override def initialDecodeState: DecodeState = Initial

  override def validateRequest(request: Request): Either[String, Request] =
    for {
      _ <- AddressValidator.validate(request.address)
    } yield request

}
