package pl.belicki.modbus.models.function

import pl.belicki.modbus.models.ExceptionCode

import java.nio.ByteBuffer

object WriteSingleRegister extends ModbusFunction(0x06) {

  case class Request(
      address: Int,
      value: Short
  ) extends super.Request

  type REQ = Request

  private object Initial extends DecodeState {
    override def decode(byteBuffer: ByteBuffer): Either[Error, DecodeState] = {
      if (byteBuffer.remaining() < 4) return ExceptionCode.ILLEGAL_DATA_VALUE
      val address = java.lang.Short.toUnsignedInt(byteBuffer.getShort)
      val value   = byteBuffer.getShort

      Right(FinalState(Request(address, value)))
    }

    override def toReq: Either[Error, Request] =
      ExceptionCode.ILLEGAL_DATA_VALUE
  }

  override def initialDecodeState: DecodeState = Initial

  override def validateRequest(request: Request): Either[String, Request] = Right(request)

}
