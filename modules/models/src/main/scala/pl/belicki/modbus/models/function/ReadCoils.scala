package pl.belicki.modbus.models.function

import java.nio.ByteBuffer

object ReadCoils extends ModbusFunction(0x01) with App {

  case class Request(
      address: Int,
      quantity: Int
  ) extends super.Request

  type REQ = Request

  private object Initial extends DecodeState {
    override def decode(byteBuffer: ByteBuffer): Either[Error, DecodeState] = {
      if (byteBuffer.remaining() < 4) return Left(Error(0x03))
      val address  = java.lang.Short.toUnsignedInt(byteBuffer.getShort)
      val quantity = java.lang.Short.toUnsignedInt(byteBuffer.getShort)

      if (!validateQuantity(quantity)) return Left(Error(0x03))

      Right(FinalState(Request(address, quantity)))
    }

    override def toReq: Either[Error, Request] = Left(Error(0x03))
  }

  private case class FinalState(
      request: Request
  ) extends DecodeState {

    override def decode(byteBuffer: ByteBuffer): Either[Error, DecodeState] =
      Left(Error(0x03))

    override def toReq: Either[Error, Request] = Right(request)
  }

  override def initialState: ReadCoils.DecodeState = Initial

  def validateQuantity(quantity: Int): Boolean = quantity < 2000

}
