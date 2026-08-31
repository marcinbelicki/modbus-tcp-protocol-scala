package pl.belicki.modbus.models.function

import pl.belicki.modbus.models.ExceptionCode
import pl.belicki.modbus.models.function.ReadFileRecord.{Request, SubRequest, validateSubRequest}
import pl.belicki.modbus.models.validator.RangeValidator

import java.nio.ByteBuffer
import scala.annotation.tailrec

object WriteFileRecord extends ModbusFunction(0x15) {

  case class SubRequest(
      fileNumber: Int,
      recordNumber: Int,
      recordData: Array[Byte]
  ) {
    lazy val size: Int = java.lang.Short.BYTES * 3 + recordData.length + java.lang.Byte.BYTES

    def encode(byteBuffer: ByteBuffer): ByteBuffer = {
      byteBuffer.put(SubRequest.referenceType)
      byteBuffer.putShort(fileNumber.toShort)
      byteBuffer.putShort(recordNumber.toShort)
      byteBuffer.putShort((recordData.length / 2).toShort)
      byteBuffer.put(recordData)
    }

    override def equals(obj: Any): Boolean = obj match {
      case that: SubRequest => fileNumber == that.fileNumber && recordNumber == that.recordNumber && recordData.sameElements(that.recordData)
      case _                => false
    }
  }

  object SubRequest {
    val referenceType: Byte = 0x06.toByte
  }

  case class Request(
      subRequests: List[SubRequest]
  ) extends super.Request {
    val requestDataLength: Int  = subRequests.map(_.size).sum
    override lazy val size: Int = requestDataLength + java.lang.Byte.BYTES

    override def encode(byteBuffer: ByteBuffer): Either[String, ByteBuffer] =
      for {
        _ <- validateRequest(this)
      } yield {
        byteBuffer.put(requestDataLength.toByte)
        subRequests.foreach(_.encode(byteBuffer))
        byteBuffer
      }
  }

  type REQ = Request

  private object Initial extends RequestDecodeState {
    override def decode(byteBuffer: ByteBuffer): Either[ModbusError, RequestDecodeState] = {
      if (byteBuffer.remaining() < 2) return ExceptionCode.ILLEGAL_DATA_VALUE
      val requestDataLength = java.lang.Byte.toUnsignedInt(byteBuffer.get())

      if (!RequestDataLengthValidator.validateBool(requestDataLength)) return ExceptionCode.ILLEGAL_DATA_VALUE
      if (requestDataLength != byteBuffer.remaining()) return ExceptionCode.ILLEGAL_DATA_VALUE

      Right(ReadSubRequests(Nil))

    }

    override def toReq: Either[ModbusError, Request] = ExceptionCode.ILLEGAL_DATA_VALUE
  }

  private case class ReadSubRequests(subRequests: List[SubRequest]) extends RequestDecodeState {
    override def decode(byteBuffer: ByteBuffer): Either[ModbusError, RequestDecodeState] = {
      if (byteBuffer.remaining() < 7) return ExceptionCode.ILLEGAL_DATA_VALUE
      if (byteBuffer.get() != SubRequest.referenceType) return ExceptionCode.ILLEGAL_DATA_VALUE

      val fileNumber = java.lang.Short.toUnsignedInt(byteBuffer.getShort)
      if (!FileNumberValidator.validateBool(fileNumber)) return ExceptionCode.ILLEGAL_DATA_VALUE

      val recordNumber = java.lang.Short.toUnsignedInt(byteBuffer.getShort)
      if (!RecordNumberValidator.validateBool(recordNumber)) return ExceptionCode.ILLEGAL_DATA_VALUE

      val recordLength = java.lang.Short.toUnsignedInt(byteBuffer.getShort)
      val byteCount    = recordLength * 2
      if (byteBuffer.remaining() < byteCount) return ExceptionCode.ILLEGAL_DATA_VALUE

      val recordData = new Array[Byte](byteCount)
      byteBuffer.get(recordData)

      val newSubRequests = SubRequest(fileNumber, recordNumber, recordData) :: subRequests
      if (!byteBuffer.hasRemaining) return Right(RequestFinalState(Request(newSubRequests.reverse)))

      Right(ReadSubRequests(newSubRequests))
    }

    override def toReq: Either[ModbusError, Request] = ExceptionCode.ILLEGAL_DATA_VALUE
  }

  override def initialRequestDecodeState: RequestDecodeState = Initial

  object FileNumberValidator extends RangeValidator(0x0001, 0xffff, "file number")
  object RecordNumberValidator extends RangeValidator(0x0000, 0x270f, "record number")
  object RequestDataLengthValidator extends RangeValidator(0x0009, 0x00fb, "request data length", "02X")

  def validateSubRequest(subRequest: SubRequest): Either[String, SubRequest] =
    for {
      _ <- FileNumberValidator.validate(subRequest.fileNumber)
      _ <- RecordNumberValidator.validate(subRequest.recordNumber)
      _ <-
        Either.cond(subRequest.recordData.length % 2 == 0, (), s"The length o the record data: ${subRequest.recordData.length} must be even number.")
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
      _ <- RequestDataLengthValidator.validate(request.requestDataLength)
      _ <- helper(request.subRequests, Nil)
    } yield request
  }

  case class SubResponse(
      fileNumber: Int,
      recordNumber: Int,
      recordData: Array[Byte]
  ) {
    lazy val size: Int = java.lang.Short.BYTES * 3 + recordData.length + java.lang.Byte.BYTES

    def encode(byteBuffer: ByteBuffer): ByteBuffer = {
      byteBuffer.put(SubRequest.referenceType)
      byteBuffer.putShort(fileNumber.toShort)
      byteBuffer.putShort(recordNumber.toShort)
      byteBuffer.putShort((recordData.length / 2).toShort)
      byteBuffer.put(recordData)
    }

    override def equals(obj: Any): Boolean = obj match {
      case that: SubRequest => fileNumber == that.fileNumber && recordNumber == that.recordNumber && recordData.sameElements(that.recordData)
      case _                => false
    }
  }

  case class Response(
      subResponses: List[SubResponse]
  ) extends super.Response {
    val requestDataLength: Int  = subResponses.map(_.size).sum
    override lazy val size: Int = requestDataLength + java.lang.Byte.BYTES

    override def encode(byteBuffer: ByteBuffer): Either[String, ByteBuffer] =
      for {
        _ <- validateResponse(this)
      } yield {
        byteBuffer.put(requestDataLength.toByte)
        subResponses.foreach(_.encode(byteBuffer))
        byteBuffer
      }
  }

  override type RES = Response

  private object InitialResponseDecode extends ResponseDecodeState {
    override def decode(byteBuffer: ByteBuffer): Either[String, ResponseDecodeState] = {
      if (byteBuffer.remaining() < 2) return Left("The number of remaining bytes must be at least 2.")
      val requestDataLength = java.lang.Byte.toUnsignedInt(byteBuffer.get())

      for {
        _ <- RequestDataLengthValidator.validate(requestDataLength)
        _ <- Either.cond(
          requestDataLength != byteBuffer.remaining(),
          (),
          s"The number of remaining bytes: ${byteBuffer.remaining()} must be equal to requestDataLength: $requestDataLength"
        )
      } yield ReadSubResponses(Nil)

    }

    override def toRes: Either[String, Response] = Left("Can't convert initial state into Response")
  }

  private case class ReadSubResponses(subResponses: List[SubResponse]) extends ResponseDecodeState {
    override def decode(byteBuffer: ByteBuffer): Either[String, ResponseDecodeState] = {
      if (byteBuffer.remaining() < 7) return Left("The number of remaining bytes must be at least 7.")
      val referenceType = byteBuffer.get
      if (referenceType != SubRequest.referenceType)
        return Left(f"The referenceType: 0x$referenceType%02X must be equal to: 0x${SubRequest.referenceType}%02X")

      val fileNumber = java.lang.Short.toUnsignedInt(byteBuffer.getShort)

      for {
        _ <- FileNumberValidator.validate(fileNumber)
        recordNumber = java.lang.Short.toUnsignedInt(byteBuffer.getShort)
        _ <- RecordNumberValidator.validate(recordNumber)
        recordLength = java.lang.Short.toUnsignedInt(byteBuffer.getShort)
        byteCount    = recordLength * 2
        _ <- Either.cond(
          byteBuffer.remaining() < byteCount,
          (),
          s"The number of remaining bytes: ${byteBuffer.remaining()} must be equal to expected byteCount: $byteCount"
        )
        recordData = new Array[Byte](byteCount)
      } yield {
        byteBuffer.get(recordData)
        val newSubResponses = SubResponse(fileNumber, recordNumber, recordData) :: subResponses
        if (byteBuffer.hasRemaining) ResponseFinalState(Response(newSubResponses))
        else ResponseFinalState(Response(newSubResponses.reverse))
      }
    }

    override def toRes: Either[String, Response] = Left("Can't convert initial state into Response - still reading the responses.")
  }

  override def initialResponseDecodeState: ResponseDecodeState = InitialResponseDecode

  def validateSubResponse(subResponse: SubResponse): Either[String, SubResponse] =
    for {
      _ <- FileNumberValidator.validate(subResponse.fileNumber)
      _ <- RecordNumberValidator.validate(subResponse.recordNumber)
      _ <-
        Either.cond(
          subResponse.recordData.length % 2 == 0,
          (),
          s"The length o the record data: ${subResponse.recordData.length} must be even number."
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
      _ <- RequestDataLengthValidator.validate(response.requestDataLength)
      _ <- helper(response.subResponses, Nil)
    } yield response
  }

}
