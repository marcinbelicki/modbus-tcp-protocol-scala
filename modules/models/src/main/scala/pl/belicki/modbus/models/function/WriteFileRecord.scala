package pl.belicki.modbus.models.function

import pl.belicki.modbus.models.ExceptionCode

import java.nio.ByteBuffer

object WriteFileRecord extends ModbusFunction(0x15) {

  case class SubRequest(
      fileNumber: Int,
      recordNumber: Int,
      recordLength: Int,
      recordData: Array[Byte]
  )

  case class Request(
      subRequests: List[SubRequest]
  ) extends super.Request

  type REQ = Request

  private object Initial extends DecodeState {
    override def decode(byteBuffer: ByteBuffer): Either[Error, DecodeState] = {
      if (byteBuffer.remaining() < 2) return Left(Error(ExceptionCode.ILLEGAL_DATA_VALUE))
      val requestDataLength = java.lang.Byte.toUnsignedInt(byteBuffer.get())

      if (requestDataLength < 0x09 || requestDataLength > 0xfb) return Left(Error(ExceptionCode.ILLEGAL_DATA_VALUE))
      if (requestDataLength != byteBuffer.remaining()) return Left(Error(ExceptionCode.ILLEGAL_DATA_VALUE))

      Right(ReadSubRequests(Nil))

    }

    override def toReq: Either[Error, Request] = Left(Error(ExceptionCode.ILLEGAL_DATA_VALUE))
  }

  private case class ReadSubRequests(subRequests: List[SubRequest]) extends DecodeState {
    override def decode(byteBuffer: ByteBuffer): Either[Error, DecodeState] = {
      if (byteBuffer.remaining() == 0) return Right(FinalState(Request(subRequests.reverse)))
      if (byteBuffer.remaining() < 7) return Left(Error(ExceptionCode.ILLEGAL_DATA_VALUE))
      if (byteBuffer.get() != 0x06) return Left(Error(ExceptionCode.ILLEGAL_DATA_VALUE))

      val fileNumber = java.lang.Short.toUnsignedInt(byteBuffer.getShort)
      if (!validateFileNumber(fileNumber)) return Left(Error(ExceptionCode.ILLEGAL_DATA_VALUE))

      val recordNumber = java.lang.Short.toUnsignedInt(byteBuffer.getShort)
      if (!validateRecordNumber(recordNumber)) return Left(Error(ExceptionCode.ILLEGAL_DATA_VALUE))

      val recordLength = java.lang.Short.toUnsignedInt(byteBuffer.getShort)
      val byteCount    = recordLength * 2
      if (byteBuffer.remaining() != byteCount) return Left(Error(ExceptionCode.ILLEGAL_DATA_VALUE))

      val recordData = new Array[Byte](byteCount)
      byteBuffer.get(recordData)

      Right(
        ReadSubRequests(
          SubRequest(fileNumber, recordNumber, recordLength, recordData) :: subRequests
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

  def validateFileNumber(fileNumber: Int): Boolean     = fileNumber >= 0x0001 && fileNumber <= 0xffff
  def validateRecordNumber(recordNumber: Int): Boolean = recordNumber <= 0x270f

}
