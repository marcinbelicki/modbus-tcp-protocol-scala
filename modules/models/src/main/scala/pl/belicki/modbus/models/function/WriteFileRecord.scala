package pl.belicki.modbus.models.function

import pl.belicki.modbus.models.ExceptionCode
import pl.belicki.modbus.models.function.ReadFileRecord.{Request, SubRequest, validateSubRequest}

import java.nio.ByteBuffer
import scala.annotation.tailrec

object WriteFileRecord extends ModbusFunction(0x15) {

  case class SubRequest(
      fileNumber: Int,
      recordNumber: Int,
      recordData: Array[Byte]
  )

  case class Request(
      subRequests: List[SubRequest]
  ) extends super.Request

  type REQ = Request

  private object Initial extends DecodeState {
    override def decode(byteBuffer: ByteBuffer): Either[Error, DecodeState] = {
      if (byteBuffer.remaining() < 2) return ExceptionCode.ILLEGAL_DATA_VALUE
      val requestDataLength = java.lang.Byte.toUnsignedInt(byteBuffer.get())

      if (requestDataLength < 0x09 || requestDataLength > 0xfb) return ExceptionCode.ILLEGAL_DATA_VALUE
      if (requestDataLength != byteBuffer.remaining()) return ExceptionCode.ILLEGAL_DATA_VALUE

      Right(ReadSubRequests(Nil))

    }

    override def toReq: Either[Error, Request] = ExceptionCode.ILLEGAL_DATA_VALUE
  }

  private case class ReadSubRequests(subRequests: List[SubRequest]) extends DecodeState {
    override def decode(byteBuffer: ByteBuffer): Either[Error, DecodeState] = {
      if (byteBuffer.remaining() == 0) return Right(FinalState(Request(subRequests.reverse)))
      if (byteBuffer.remaining() < 7) return ExceptionCode.ILLEGAL_DATA_VALUE
      if (byteBuffer.get() != 0x06) return ExceptionCode.ILLEGAL_DATA_VALUE

      val fileNumber = java.lang.Short.toUnsignedInt(byteBuffer.getShort)
      if (!validateFileNumber(fileNumber)) return ExceptionCode.ILLEGAL_DATA_VALUE

      val recordNumber = java.lang.Short.toUnsignedInt(byteBuffer.getShort)
      if (!validateRecordNumber(recordNumber)) return ExceptionCode.ILLEGAL_DATA_VALUE

      val recordLength = java.lang.Short.toUnsignedInt(byteBuffer.getShort)
      val byteCount    = recordLength * 2
      if (byteBuffer.remaining() != byteCount) return ExceptionCode.ILLEGAL_DATA_VALUE

      val recordData = new Array[Byte](byteCount)
      byteBuffer.get(recordData)

      Right(
        ReadSubRequests(
          SubRequest(fileNumber, recordNumber, recordData) :: subRequests
        )
      )
    }

    override def toReq: Either[Error, Request] = ExceptionCode.ILLEGAL_DATA_VALUE
  }

  override def initialDecodeState: DecodeState = Initial

  def validateFileNumber(fileNumber: Int): Boolean     = fileNumber >= 0x0001 && fileNumber <= 0xffff
  def validateRecordNumber(recordNumber: Int): Boolean = recordNumber >= 0x0000 && recordNumber <= 0x270f

  def validateSubRequest(subRequest: SubRequest): Either[String, SubRequest] = {
    if (!validateFileNumber(subRequest.fileNumber))
      return Left(s"The file number: ${subRequest.fileNumber} of the request must be inside of the range <0x0001;0xffff>")
    if (!validateRecordNumber(subRequest.recordNumber))
      return Left(s"The record number: ${subRequest.recordNumber} of the request must be inside of the range <0x0;0x270f")
    if (subRequest.recordData.length % 2 != 0)
      return Left(s"The length of the record data must be even number.")

    Right(subRequest)
  }

  override def validateRequest(request: Request): Either[String, Request] = {
    @tailrec
    def helper(subRequests: List[SubRequest], errors: List[String]): Either[String, Request] =
      subRequests match {
        case head :: tail => validateSubRequest(head) match {
            case Right(_)    => helper(tail, errors)
            case Left(error) => helper(tail, error :: errors)
          }
        case _ =>
          if (errors.isEmpty) Right(request) else Left(errors.mkString(System.lineSeparator()))
      }

    helper(request.subRequests, Nil)
  }

}
