package pl.belicki.modbus.models.function

import pl.belicki.modbus.models.ExceptionCode

import java.nio.ByteBuffer

object ReadCoils extends ModbusFunction(0x01) with App {

  case class Request(
      address: Int,
      quantity: Int
  ) extends super.Request

  type REQ = Request

  private object Initial extends DecodeState {
    override def decode(byteBuffer: ByteBuffer): Either[Error, DecodeState] = {
      if (byteBuffer.remaining() < 4) return Left(Error(ExceptionCode.ILLEGAL_DATA_VALUE))
      val address  = java.lang.Short.toUnsignedInt(byteBuffer.getShort)
      val quantity = java.lang.Short.toUnsignedInt(byteBuffer.getShort)

      if (!validateQuantity(quantity)) return Left(Error(ExceptionCode.ILLEGAL_DATA_VALUE))

      Right(FinalState(Request(address, quantity)))
    }

    override def toReq: Either[Error, Request] = Left(Error(ExceptionCode.ILLEGAL_DATA_VALUE))
  }

  private case class FinalState(
      request: Request
  ) extends DecodeState {

    override def decode(byteBuffer: ByteBuffer): Either[Error, DecodeState] =
      Left(Error(ExceptionCode.ILLEGAL_DATA_VALUE))

    override def toReq: Either[Error, Request] = Right(request)
  }

  override def initialState: ReadCoils.DecodeState = Initial

  def validateQuantity(quantity: Int): Boolean = quantity < 2000

}
