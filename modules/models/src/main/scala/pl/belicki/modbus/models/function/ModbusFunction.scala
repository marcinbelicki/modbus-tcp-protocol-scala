package pl.belicki.modbus.models.function

import pl.belicki.modbus.models.ExceptionCode

import java.nio.ByteBuffer
import scala.annotation.tailrec
import scala.language.implicitConversions

abstract class ModbusFunction(_code: Int) {
  val code: Byte = _code.toByte

  abstract class Request {
    val function: ModbusFunction = ModbusFunction.this
  }

  abstract class Response {
    val function: ModbusFunction = ModbusFunction.this
  }

  case class Error(exceptionCode: ExceptionCode) {
    val functionCode: Byte = Error.code
  }

  object Error {
    val code: Byte                                                               = (ModbusFunction.this.code + 0x80).toByte
    implicit def toError(exceptionCode: ExceptionCode): Error                    = apply(exceptionCode)
    implicit def toLeftError(exceptionCode: ExceptionCode): Left[Error, Nothing] = toLeft(exceptionCode)
    implicit def toLeft(error: Error): Left[Error, Nothing]                      = Left(error)
  }

  type REQ <: Request

  trait DecodeState {
    def decode(byteBuffer: ByteBuffer): Either[Error, DecodeState]

    def toReq: Either[Error, REQ]
  }

  def initialDecodeState: DecodeState = ???

  final def decodeRequest(byteBuffer: ByteBuffer): Either[Error, REQ] = {
    @tailrec
    def helper(state: DecodeState): Either[Error, REQ] =
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
