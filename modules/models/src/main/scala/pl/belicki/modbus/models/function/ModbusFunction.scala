package pl.belicki.modbus.models.function

import pl.belicki.modbus.models.ExceptionCode
import pl.belicki.modbus.models.util.SpacedHex
import pl.belicki.modbus.models.validator.RangeValidator

import java.nio.ByteBuffer
import scala.annotation.tailrec
import scala.language.{existentials, implicitConversions}

abstract class ModbusFunction(_code: Int) {
  val code: Byte = _code.toByte

  abstract class Request {
    val function: ModbusFunction = ModbusFunction.this

    def size: Int
    def encode(byteBuffer: ByteBuffer): Either[String, ByteBuffer]

    private lazy val fullSize = size + java.lang.Byte.BYTES

    def toByteBuffer: Either[String, ByteBuffer] =
      for {
        _ <- RequestSizeValidator.validate(fullSize)
        byteBuffer = ByteBuffer.allocate(fullSize)
        _          = byteBuffer.put(code)
        byteBuffer <- encode(byteBuffer)
      } yield byteBuffer

  }

  object RequestSizeValidator extends RangeValidator(0x0000, 0x00fd, "size")

  abstract class Response {
    val function: ModbusFunction = ModbusFunction.this
  }

  def error(exceptionCode: ExceptionCode) =
    ModbusError(exceptionCode = exceptionCode, functionCode = errorCode)

  lazy val errorCode: Byte                                        = (ModbusFunction.this.code + ModbusRequestDecoder.ERROR_CODE_ADDITION).toByte
  implicit def toError(exceptionCode: ExceptionCode): ModbusError = error(exceptionCode)
  implicit def toLeftError(exceptionCode: ExceptionCode): Left[ModbusError, Nothing]      = toLeft(exceptionCode)
  implicit def toLeft(error: ModbusError): Left[ModbusError, Nothing]                     = Left(error)
  implicit def toEitherError[R](either: Either[ExceptionCode, R]): Either[ModbusError, R] = either.left.map(toError)

  type REQ <: Request

  trait DecodeState {
    def decode(byteBuffer: ByteBuffer): Either[ModbusError, DecodeState]

    def toReq: Either[ModbusError, REQ]
  }

  protected case class FinalState(request: REQ) extends DecodeState {
    override def decode(byteBuffer: ByteBuffer): Either[ModbusError, DecodeState] = ExceptionCode.ILLEGAL_DATA_VALUE

    override def toReq: Either[ModbusError, REQ] = Right(request)
  }

  def initialDecodeState: DecodeState
  def validateRequest(request: REQ): Either[String, REQ]

  final def decodeHexRequest(hex: String): Either[ModbusError, REQ]     = decodeRequest(SpacedHex.parseHex(hex))
  final def decodeRequest(bytes: Array[Byte]): Either[ModbusError, REQ] = decodeRequest(ByteBuffer.wrap(bytes))

  final def decodeRequest(byteBuffer: ByteBuffer): Either[ModbusError, REQ] = {
    @tailrec
    def helper(state: DecodeState): Either[ModbusError, REQ] =
      if (byteBuffer.remaining() <= 0) state.toReq
      else {
        state.decode(byteBuffer) match {
          case Right(newState) => helper(newState)
          case Left(error)     => Left(error)
        }
      }

    helper(initialDecodeState)
  }

}
