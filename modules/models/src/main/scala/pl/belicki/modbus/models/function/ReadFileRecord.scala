package pl.belicki.modbus.models.function

import pl.belicki.modbus.models.ExceptionCode

import java.nio.ByteBuffer
import scala.annotation.tailrec

object ReadFileRecord extends ModbusFunction(0x14) {

  case class SubRequest(
      fileNumber: Int,
      recordNumber: Int,
      recordLength: Int
  )

  case class Request(
      subRequests: List[SubRequest]
  ) extends super.Request

  type REQ = Request

  private object Initial extends DecodeState {
    override def decode(byteBuffer: ByteBuffer): Either[Error, DecodeState] = {
      if (byteBuffer.remaining() < 8) return Left(Error(ExceptionCode.ILLEGAL_DATA_VALUE))
      val byteCount = java.lang.Byte.toUnsignedInt(byteBuffer.get())
      if ((byteCount % 7) != 0) return Left(Error(ExceptionCode.ILLEGAL_DATA_VALUE))

      val subRequestCount = byteCount / 7
      if (byteBuffer.remaining() != byteCount) return Left(Error(ExceptionCode.ILLEGAL_DATA_VALUE))

      Right(ReadingSubRequests(subRequestCount))

    }

    override def toReq: Either[Error, Request] = Left(Error(ExceptionCode.ILLEGAL_DATA_VALUE))
  }

  private case class ReadingSubRequests(subRequestCount: Int) extends DecodeState {

    private def toRequest(subRequests: LazyList[Either[Error, SubRequest]]) = {
      @tailrec
      def helper(subRequests: LazyList[Either[Error, SubRequest]], acc: List[SubRequest]): Either[Error, Request] =
        subRequests match {
          case Left(error) #:: _          => Left(error)
          case Right(subRequest) #:: tail => helper(tail, subRequest :: acc)
          case _                          => Right(Request(acc.reverse))
        }

      helper(subRequests, Nil)
    }

    private def readSubRequest(byteBuffer: ByteBuffer): Either[Error, SubRequest] = {
      if (byteBuffer.get() != 0x06) return Left(Error(ExceptionCode.ILLEGAL_DATA_VALUE))

      val fileNumber = java.lang.Short.toUnsignedInt(byteBuffer.getShort)
      if (!validateFileNumber(fileNumber)) return Left(Error(ExceptionCode.ILLEGAL_DATA_VALUE))

      val recordNumber = java.lang.Short.toUnsignedInt(byteBuffer.getShort)
      if (!validateRecordNumber(recordNumber)) return Left(Error(ExceptionCode.ILLEGAL_DATA_VALUE))

      val recordLength = java.lang.Short.toUnsignedInt(byteBuffer.getShort())

      Right(SubRequest(fileNumber, recordNumber, recordLength))
    }
    override def decode(byteBuffer: ByteBuffer): Either[Error, DecodeState] = {
      val subRequests = LazyList.fill(subRequestCount)(readSubRequest(byteBuffer))

      toRequest(subRequests)
        .map(FinalState)
    }

    override def toReq: Either[Error, Request] = Left(Error(ExceptionCode.ILLEGAL_DATA_VALUE))
  }

  private case class FinalState(request: Request) extends DecodeState {
    override def decode(byteBuffer: ByteBuffer): Either[Error, DecodeState] = Left(Error(ExceptionCode.ILLEGAL_DATA_VALUE))

    override def toReq: Either[Error, Request] = Right(request)
  }

  override def initialDecodeState: DecodeState = Initial

  def validateFileNumber(fileNumber: Int): Boolean     = fileNumber >= 0x0001 && fileNumber <= 0xffff
  def validateRecordNumber(recordNumber: Int): Boolean = recordNumber >= 0x0000 && recordNumber <= 0x270f

}
