package pl.belicki.modbus.models.function

import pl.belicki.modbus.models.ExceptionCode

import java.nio.ByteBuffer

object ReadFIFOQueue extends ModbusFunction(0x18) {

  case class Request(
      address: Int
  ) extends super.Request

  override type REQ = Request

  private case object Initial extends DecodeState {
    override def decode(byteBuffer: ByteBuffer): Either[Error, DecodeState] = {
      if (byteBuffer.remaining() != 2) return ExceptionCode.ILLEGAL_DATA_VALUE
      val address = java.lang.Short.toUnsignedInt(byteBuffer.getShort)
      Right(FinalState(Request(address)))
    }

    override def toReq: Either[Error, Request] = ExceptionCode.ILLEGAL_DATA_VALUE
  }

  override def initialDecodeState: ReadFIFOQueue.DecodeState = Initial

  override def validateRequest(request: Request): Either[String, Request] = {
    if (request.address > 0xffff || request.address < 0x0000)
      return Left(s"The address of the request: ${request.address} must be inside of the range: <0x0000;0xffff>")

    Right(request)
  }
}
