package pl.belicki.modbus.models.function

import pl.belicki.modbus.models.ExceptionCode

import java.nio.ByteBuffer

object MaskWriteRegister extends ModbusFunction(0x16) {

  case class Request(
      address: Int,
      andMask: Short,
      orMask: Short
  ) extends super.Request

  type REQ = Request

  private object Initial extends DecodeState {
    override def decode(byteBuffer: ByteBuffer): Either[Error, DecodeState] = {
      if (byteBuffer.remaining() != 6) return Left(Error(ExceptionCode.ILLEGAL_DATA_VALUE))
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

    override def toReq: Either[Error, Request] = Left(Error(ExceptionCode.ILLEGAL_DATA_VALUE))
  }

  private case class FinalState(request: Request) extends DecodeState {
    override def decode(byteBuffer: ByteBuffer): Either[Error, DecodeState] =
      Left(Error(ExceptionCode.ILLEGAL_DATA_VALUE))

    override def toReq: Either[Error, Request] = Right(request)
  }

  override def initialDecodeState: DecodeState = Initial

}
