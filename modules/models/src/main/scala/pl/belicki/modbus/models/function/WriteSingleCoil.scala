package pl.belicki.modbus.models.function

import pl.belicki.modbus.models.ExceptionCode

import java.nio.ByteBuffer

object WriteSingleCoil extends ModbusFunction(0x05) {

  case class Request(
      address: Int,
      value: Boolean
  ) extends super.Request

  type REQ = Request

  private object Initial extends DecodeState {
    private val valueMap = Map(
      0xff00.toShort -> true,
      0x0000.toShort -> false
    )
    override def decode(byteBuffer: ByteBuffer): Either[Error, DecodeState] = {
      if (byteBuffer.remaining() < 4) return ExceptionCode.ILLEGAL_DATA_VALUE
      val address = java.lang.Short.toUnsignedInt(byteBuffer.getShort)
      for {
        value <- valueMap.get(byteBuffer.getShort).toRight(Error(ExceptionCode.ILLEGAL_DATA_VALUE))
      } yield FinalState(Request(address, value))
    }

    override def toReq: Either[Error, Request] = ExceptionCode.ILLEGAL_DATA_VALUE
  }

  override def initialDecodeState: DecodeState = Initial

  override def validateRequest(request: Request): Either[String, Request] = Right(request)
}
