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

  private object Initial extends RequestDecodeState {
    override def decode(byteBuffer: ByteBuffer): Either[ModbusError, RequestDecodeState] = {
      if (byteBuffer.remaining() < 8) return ExceptionCode.ILLEGAL_DATA_VALUE
      val byteCount = java.lang.Byte.toUnsignedInt(byteBuffer.get())
      if ((byteCount % 7) != 0) return ExceptionCode.ILLEGAL_DATA_VALUE
      if (!SubRequestsSizeValidator.validateBool(byteCount)) return ExceptionCode.ILLEGAL_DATA_VALUE

      val subRequestCount = byteCount / 7
      if (byteBuffer.remaining() != byteCount) return ExceptionCode.ILLEGAL_DATA_VALUE

      if (subRequestCount == 0) return Right(RequestFinalState(Request(Nil)))

      Right(ReadingSubRequests(subRequestCount, Nil))
    }

    override def toReq: Either[ModbusError, Request] = ExceptionCode.ILLEGAL_DATA_VALUE
  }

  private case class ReadingSubRequests(subRequestCount: Int, subRequests: List[SubRequest]) extends RequestDecodeState {

    override def decode(byteBuffer: ByteBuffer): Either[ModbusError, RequestDecodeState] = {
      if (byteBuffer.get() != SubRequest.referenceType) return ExceptionCode.ILLEGAL_DATA_VALUE

      val fileNumber = java.lang.Short.toUnsignedInt(byteBuffer.getShort)
      if (!FileNumberValidator.validateBool(fileNumber)) return ExceptionCode.ILLEGAL_DATA_VALUE

      val recordNumber = java.lang.Short.toUnsignedInt(byteBuffer.getShort)
      if (!RecordNumberValidator.validateBool(recordNumber)) return ExceptionCode.ILLEGAL_DATA_VALUE

      val recordLength = java.lang.Short.toUnsignedInt(byteBuffer.getShort())

      if (subRequestCount == 1) return Right(RequestFinalState(Request((SubRequest(fileNumber, recordNumber, recordLength) :: subRequests).reverse)))

      Right(ReadingSubRequests(subRequestCount - 1, SubRequest(fileNumber, recordNumber, recordLength) :: subRequests))
    }

    override def toReq: Either[ModbusError, Request] = ExceptionCode.ILLEGAL_DATA_VALUE
  }

  override def initialRequestDecodeState: RequestDecodeState = Initial

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
          if (errors.isEmpty) Right(request) else Left(errors.reverse.mkString(System.lineSeparator()))
      }

    for {
      _ <- SubRequestsSizeValidator.validate(request.subRequestsSize)
      _ <- helper(request.subRequests, Nil)
    } yield request
  }

  case class SubResponse(
      recordData: Array[Byte]
  ) {
    lazy val fileRespLength: Int = recordData.length + java.lang.Byte.BYTES

    lazy val size: Int = recordData.length + java.lang.Byte.BYTES * 2

    def encode(byteBuffer: ByteBuffer): ByteBuffer = {
      byteBuffer.put(fileRespLength.toByte)
      byteBuffer.put(referenceType)
      byteBuffer.put(recordData)
    }

  }

  case class Response(
      subResponses: List[SubResponse]
  ) extends super.Response {

    val respDataLength: Int = subResponses.map(_.size).sum

    override lazy val size: Int = respDataLength + java.lang.Byte.SIZE

    override def encode(byteBuffer: ByteBuffer): Either[String, ByteBuffer] = for {
      _ <- validateResponse(this)
    } yield {
      byteBuffer.put(respDataLength.toByte)
      subResponses.foreach(_.encode(byteBuffer))
      byteBuffer
    }
  }

  override type RES = Response

  private val fileRespLengthRangeValidator = new RangeValidator(0x07, 0xf5, "fileRespLength", "02X")
  private val respDataLengthRangeValidator = new RangeValidator(0x07, 0xf5, "respDataLength", "02X")

  def validateSubResponse(subResponse: SubResponse): Either[String, SubResponse] =
    for {
      _ <- fileRespLengthRangeValidator.validate(subResponse.fileRespLength)
      _ <- Either.cond(
        subResponse.recordData.length % 2 == 0,
        (),
        s"The length of the recordData: ${subResponse.recordData.length} must be an even number"
      )
    } yield subResponse

  override def validateResponse(response: Response): Either[String, Response] = {

    @tailrec
    def helper(subResponses: List[SubResponse], errors: List[String]): Either[String, Response] =
      subResponses match {
        case head :: tail => validateSubResponse(head) match {
            case Right(_)    => helper(tail, errors)
            case Left(error) => helper(tail, error :: errors)
          }
        case _ =>
          if (errors.isEmpty) Right(response) else Left(errors.reverse.mkString(System.lineSeparator()))
      }

    for {
      _ <- respDataLengthRangeValidator.validate(response.respDataLength)
      _ <- helper(response.subResponses, Nil)
    } yield response
  }

  private object InitialResponseDecode extends ResponseDecodeState {
    override def decode(byteBuffer: ByteBuffer): Either[String, ResponseDecodeState] = {
      val respDataLength = java.lang.Byte.toUnsignedInt(byteBuffer.get())
      for {
        _ <- respDataLengthRangeValidator.validate(respDataLength)
        _ <- Either.cond(
          respDataLength == byteBuffer.remaining(),
          (),
          s"The number of remaining bytes: ${byteBuffer.remaining()} must be equal to respDataLength: $respDataLength."
        )
      } yield ReadingSubResponses(Nil)

    }

    override def toRes: Either[String, Response] = Left("Can't convert initial state into Response")
  }

  private case class ReadingSubResponses(subResponses: List[SubResponse]) extends ResponseDecodeState {

    override def decode(byteBuffer: ByteBuffer): Either[String, ResponseDecodeState] = {
      if (byteBuffer.remaining() < 2) return Left("Too little bytes to read sub response")

      val fileRespLength = byteBuffer.get

      for {
        _ <- fileRespLengthRangeValidator.validate(fileRespLength)
        _ <- Either.cond(
          fileRespLength <= byteBuffer.remaining(),
          (),
          s"Too little bytes too read sub response after reading fileRespLength: $fileRespLength."
        )
        foundReferenceType = byteBuffer.get
        _ <- Either.cond(foundReferenceType == referenceType, (), s"The reference type: $foundReferenceType must be equal to $referenceType.")
        remainingBytesOfSubResponse = fileRespLength - 1
        _ <- Either.cond(remainingBytesOfSubResponse % 2 == 0, (), s"The number of bytes of the record data must be an even number.")
        recordData = new Array[Byte](remainingBytesOfSubResponse)

      } yield {
        byteBuffer.get(recordData)

        if (byteBuffer.remaining() == 0) ResponseFinalState(Response((SubResponse(recordData) :: subResponses).reverse))
        else ReadingSubResponses(SubResponse(recordData) :: subResponses)
      }
    }

    override def toRes: Either[String, Response] = Left("Can't convert to Response from ReadingSubResponses state.")
  }

  override def initialResponseDecodeState: ResponseDecodeState = InitialResponseDecode
}
