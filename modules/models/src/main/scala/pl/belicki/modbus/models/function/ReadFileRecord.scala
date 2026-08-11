package pl.belicki.modbus.models.function

import pl.belicki.modbus.models.ExceptionCode
import pl.belicki.modbus.models.function.ReadFileRecord.SubRequest.referenceType
import pl.belicki.modbus.models.validator.RangeValidator

import java.nio.ByteBuffer
import scala.annotation.tailrec

object ReadFileRecord extends ModbusFunction(0x14) {

  case class SubRequest(
      fileNumber: Int,
      recordNumber: Int,
      recordLength: Int
  ) {
    def encode(byteBuffer: ByteBuffer): ByteBuffer = {
      byteBuffer.put(referenceType)
      byteBuffer.putShort(fileNumber.toShort)
      byteBuffer.putShort(recordNumber.toShort)
      byteBuffer.putShort(recordLength.toShort)
    }
  }

  object SubRequest {
    val size: Int           = java.lang.Short.BYTES * 3 + java.lang.Byte.BYTES
    val referenceType: Byte = 0x06.toByte
  }

  case class Request(
      subRequests: List[SubRequest]
  ) extends super.Request {
    lazy val subRequestsSize: Int = subRequests.length * SubRequest.size
    override lazy val size: Int   = subRequestsSize + java.lang.Byte.BYTES

    override def encode(byteBuffer: ByteBuffer): Either[String, ByteBuffer] =
      for {
        _ <- validateRequest(this)
      } yield {
        byteBuffer.put(subRequestsSize.toByte)
        subRequests.foreach(_.encode(byteBuffer))
        byteBuffer
      }

  }

  type REQ = Request

  private object Initial extends DecodeState {
    override def decode(byteBuffer: ByteBuffer): Either[ModbusError, DecodeState] = {
      if (byteBuffer.remaining() < 8) return ExceptionCode.ILLEGAL_DATA_VALUE
      val byteCount = java.lang.Byte.toUnsignedInt(byteBuffer.get())
      if ((byteCount % 7) != 0) return ExceptionCode.ILLEGAL_DATA_VALUE
      if (!SubRequestsSizeValidator.validateBool(byteCount)) return ExceptionCode.ILLEGAL_DATA_VALUE

      val subRequestCount = byteCount / 7
      if (byteBuffer.remaining() != byteCount) return ExceptionCode.ILLEGAL_DATA_VALUE

      if (subRequestCount == 0) return Right(FinalState(Request(Nil)))

      Right(ReadingSubRequests(subRequestCount, Nil))
    }

    override def toReq: Either[ModbusError, Request] = ExceptionCode.ILLEGAL_DATA_VALUE
  }

  private case class ReadingSubRequests(subRequestCount: Int, subRequests: List[SubRequest]) extends DecodeState {

    override def decode(byteBuffer: ByteBuffer): Either[ModbusError, DecodeState] = {
      if (byteBuffer.get() != SubRequest.referenceType) return ExceptionCode.ILLEGAL_DATA_VALUE

      val fileNumber = java.lang.Short.toUnsignedInt(byteBuffer.getShort)
      if (!FileNumberValidator.validateBool(fileNumber)) return ExceptionCode.ILLEGAL_DATA_VALUE

      val recordNumber = java.lang.Short.toUnsignedInt(byteBuffer.getShort)
      if (!RecordNumberValidator.validateBool(recordNumber)) return ExceptionCode.ILLEGAL_DATA_VALUE

      val recordLength = java.lang.Short.toUnsignedInt(byteBuffer.getShort())

      if (subRequestCount == 1) return Right(FinalState(Request((SubRequest(fileNumber, recordNumber, recordLength) :: subRequests).reverse)))

      Right(ReadingSubRequests(subRequestCount - 1, SubRequest(fileNumber, recordNumber, recordLength) :: subRequests))
    }

    override def toReq: Either[ModbusError, Request] = ExceptionCode.ILLEGAL_DATA_VALUE
  }

  override def initialDecodeState: DecodeState = Initial

  object FileNumberValidator extends RangeValidator(0x0001, 0xffff, "file number")
  object RecordNumberValidator extends RangeValidator(0x0000, 0x270f, "record number")
  object RecordLengthValidator extends RangeValidator(0x0000, 0xffff, "record number")
  object SubRequestsSizeValidator extends RangeValidator(0x07, 0xf5, "sub requests size", "02X")

  def validateSubRequest(subRequest: SubRequest): Either[String, SubRequest] =
    for {
      _ <- FileNumberValidator.validate(subRequest.fileNumber)
      _ <- RecordNumberValidator.validate(subRequest.recordNumber)
      _ <- RecordLengthValidator.validate(subRequest.recordLength)
    } yield subRequest

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

    for {
      _ <- SubRequestsSizeValidator.validate(request.subRequestsSize)
      _ <- helper(request.subRequests.toList, Nil)
    } yield request
  }
}
