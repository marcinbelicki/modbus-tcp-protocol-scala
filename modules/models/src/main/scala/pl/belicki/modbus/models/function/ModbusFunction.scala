package pl.belicki.modbus.models.function

import pl.belicki.modbus.models.ExceptionCode
import pl.belicki.modbus.models.util.SpacedHex
import pl.belicki.modbus.models.validator.RangeValidator

import java.nio.ByteBuffer
import scala.annotation.tailrec
import scala.language.{existentials, implicitConversions}

abstract class ModbusFunction(_code: Int) {
  val code: Byte = _code.toByte

  abstract class Message {
    val function: ModbusFunction = ModbusFunction.this

    def size: Int
    def encode(byteBuffer: ByteBuffer): Either[String, ByteBuffer]

    private lazy val fullSize = size + java.lang.Byte.BYTES

    def toByteBuffer: Either[String, ByteBuffer] =
      for {
        _ <- MessageSizeValidator.validate(fullSize)
        byteBuffer = ByteBuffer.allocate(fullSize)
        _          = byteBuffer.put(code)
        byteBuffer <- encode(byteBuffer)
      } yield byteBuffer
  }
  abstract class Request extends Message

  object MessageSizeValidator extends RangeValidator(0x0000, 0x00fd, "size")

  abstract class Response extends Message

  def error(exceptionCode: ExceptionCode) =
    ModbusError(exceptionCode = exceptionCode, functionCode = errorCode)

  lazy val errorCode: Byte                                        = (ModbusFunction.this.code + ModbusRequestDecoder.ERROR_CODE_ADDITION).toByte
  implicit def toError(exceptionCode: ExceptionCode): ModbusError = error(exceptionCode)
  implicit def toLeftError(exceptionCode: ExceptionCode): Left[ModbusError, Nothing]      = toLeft(exceptionCode)
  implicit def toLeft(error: ModbusError): Left[ModbusError, Nothing]                     = Left(error)
  implicit def toEitherError[R](either: Either[ExceptionCode, R]): Either[ModbusError, R] = either.left.map(toError)

  type REQ <: Request
  type RES <: Response

  trait RequestDecodeState {
    def decode(byteBuffer: ByteBuffer): Either[ModbusError, RequestDecodeState]

    def toReq: Either[ModbusError, REQ]
  }

  protected case class RequestFinalState(request: REQ) extends RequestDecodeState {
    override def decode(byteBuffer: ByteBuffer): Either[ModbusError, RequestDecodeState] = ExceptionCode.ILLEGAL_DATA_VALUE

    override def toReq: Either[ModbusError, REQ] = Right(request)
  }

  def initialRequestDecodeState: RequestDecodeState
  def validateRequest(request: REQ): Either[String, REQ]

  final def decodeHexRequest(hex: String): Either[ModbusError, REQ]     = decodeRequest(SpacedHex.parseHex(hex))
  final def decodeRequest(bytes: Array[Byte]): Either[ModbusError, REQ] = decodeRequest(ByteBuffer.wrap(bytes))

  final def decodeRequest(byteBuffer: ByteBuffer): Either[ModbusError, REQ] = {
    @tailrec
    def helper(state: RequestDecodeState): Either[ModbusError, REQ] =
      if (byteBuffer.remaining() <= 0) state.toReq
      else {
        state.decode(byteBuffer) match {
          case Right(newState) => helper(newState)
          case Left(error)     => Left(error)
        }
      }

    helper(initialRequestDecodeState)
  }

  trait ResponseDecodeState {
    def decode(byteBuffer: ByteBuffer): Either[String, ResponseDecodeState]

    def toRes: Either[String, RES]
  }

  protected case class ResponseFinalState(response: RES) extends ResponseDecodeState {
    override def decode(byteBuffer: ByteBuffer): Either[String, ResponseDecodeState] =
      Left("Tried to read another bytes even though the decoder reached final state.")

    override def toRes: Either[String, RES] = Right(response)
  }

  def initialResponseDecodeState: ResponseDecodeState
  def validateResponse(response: RES): Either[String, RES]

  final def decodeHexResponse(hex: String): Either[String, RES]     = decodeResponse(SpacedHex.parseHex(hex))
  final def decodeResponse(bytes: Array[Byte]): Either[String, RES] = decodeResponse(ByteBuffer.wrap(bytes))

  final def decodeResponse(byteBuffer: ByteBuffer): Either[String, RES] = {
    @tailrec
    def helper(state: ResponseDecodeState): Either[String, RES] =
      if (byteBuffer.remaining() <= 0) state.toRes
      else {
        state.decode(byteBuffer) match {
          case Right(newState) => helper(newState)
          case Left(error)     => Left(error)
        }
      }

    helper(initialResponseDecodeState)
  }

}
