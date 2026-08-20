package pl.belicki.modbus.models.function

import pl.belicki.modbus.models.ExceptionCode
import pl.belicki.modbus.models.validator.RangeValidator

import java.nio.ByteBuffer

object ReadFIFOQueue extends ModbusFunction(0x18) {

  case class Request(
      address: Int
  ) extends super.Request {
    override def size: Int = java.lang.Short.BYTES

    override def encode(byteBuffer: ByteBuffer): Either[String, ByteBuffer] =
      for {
        _ <- validateRequest(this)
      } yield byteBuffer.putShort(address.toShort)
  }

  override type REQ = Request

  private case object Initial extends RequestDecodeState {
    override def decode(byteBuffer: ByteBuffer): Either[ModbusError, RequestDecodeState] = {
      if (byteBuffer.remaining() != 2) return ExceptionCode.ILLEGAL_DATA_VALUE
      val address = java.lang.Short.toUnsignedInt(byteBuffer.getShort)
      Right(RequestFinalState(Request(address)))
    }

    override def toReq: Either[ModbusError, Request] = ExceptionCode.ILLEGAL_DATA_VALUE
  }

  override def initialRequestDecodeState: RequestDecodeState = Initial

  object AddressValidator extends RangeValidator(0x0000, 0xffff, "address")

  override def validateRequest(request: Request): Either[String, Request] =
    for {
      _ <- AddressValidator.validate(request.address)
    } yield request

  case class Response(
      fifoValueRegister: Array[Byte]
  ) extends super.Response {
    override def size: Int = fifoValueRegister.length + java.lang.Short.BYTES * 2

    override def encode(byteBuffer: ByteBuffer): Either[String, ByteBuffer] = for {
      _ <- validateResponse(this)
    } yield {
      val byteCount = fifoValueRegister.length + java.lang.Short.BYTES
      byteBuffer.putShort(byteCount.toShort)
      val fifoCount = fifoValueRegister.length / 2
      byteBuffer.putShort(fifoCount.toShort)
    }
  }

  override type RES = Response

  private val fifoValueRegisterRangeValidator = new RangeValidator(0, 31, "FIFO count")

  override def validateResponse(response: Response): Either[String, Response] = for {
    _ <- Either.cond(response.fifoValueRegister.length % 2 == 0, (), "The length of the fifo value register must be an even number.")
    fifoCount = response.fifoValueRegister.length / 2
    _ <- fifoValueRegisterRangeValidator.validate(fifoCount)
  } yield response

  private object InitialResponseDecode extends ResponseDecodeState {
    override def decode(byteBuffer: ByteBuffer): Either[String, ResponseDecodeState] = {
      if (byteBuffer.remaining() < 2) return Left("Too little remaining bytes for ReadFIFOQueue response.")
      val byteCount = java.lang.Short.toUnsignedInt(byteBuffer.getShort)

      if (byteBuffer.remaining() != byteCount) return Left(s"The remaining bytes: ${byteBuffer.remaining()} must be equal to $byteCount.")
      val fifoCount = java.lang.Short.toUnsignedInt(byteBuffer.getShort)

      for {
        _ <- fifoValueRegisterRangeValidator.validate(fifoCount)
        fifoValueRegisterLength = fifoCount * 2
        _ <- Either.cond(byteBuffer.remaining() == fifoValueRegisterLength, (), s"The remaining bytes: ${byteBuffer.remaining()} must be equal to $fifoValueRegisterLength.")
      } yield  {
        val fifoValueRegister = new Array[Byte](fifoValueRegisterLength)
        byteBuffer.get(fifoValueRegister)
        ResponseFinalState(Response(fifoValueRegister))
      }
    }

    override def toRes: Either[String, Response] = Left("Cannot convert from initial state into response.")
  }

  override def initialResponseDecodeState: ResponseDecodeState = InitialResponseDecode
}
