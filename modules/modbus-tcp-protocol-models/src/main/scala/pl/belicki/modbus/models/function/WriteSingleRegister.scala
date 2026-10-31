package pl.belicki.modbus.models.function

import pl.belicki.modbus.models.ExceptionCode
import pl.belicki.modbus.models.validator.RangeValidator

import java.nio.ByteBuffer

object WriteSingleRegister extends ModbusFunction(0x06) {

  case class Request(
      address: Int,
      value: Short
  ) extends super.Request {
    override def size: Int = Request.size

    override def encode(byteBuffer: ByteBuffer): Either[String, ByteBuffer] = for {
      _ <- validateRequest(this)
    } yield {
      byteBuffer.putShort(address.toShort)
      byteBuffer.putShort(value)
    }
  }

  object Request {
    val size: Int = java.lang.Short.BYTES * 2
  }

  type REQ = Request

  private object Initial extends RequestDecodeState {
    override def decode(byteBuffer: ByteBuffer): Either[ModbusError, RequestDecodeState] = {
      if (byteBuffer.remaining() < Request.size) return ExceptionCode.ILLEGAL_DATA_VALUE
      val address = java.lang.Short.toUnsignedInt(byteBuffer.getShort)
      val value   = byteBuffer.getShort

      Right(RequestFinalState(Request(address, value)))
    }

    override def toReq: Either[ModbusError, Request] =
      ExceptionCode.ILLEGAL_DATA_VALUE
  }

  override def initialRequestDecodeState: RequestDecodeState = Initial

  object AddressValidator extends RangeValidator(0x0000, 0xffff, "address")

  override def validateRequest(request: Request): Either[String, Request] =
    for {
      _ <- AddressValidator.validate(request.address)
    } yield request

  case class Response(address: Int, value: Short) extends super.Response {
    override def size: Int = Response.size

    override def encode(byteBuffer: ByteBuffer): Either[String, ByteBuffer] =
      for {
        _ <- validateResponse(this)
      } yield {
        byteBuffer.putShort(address.toShort)
        byteBuffer.putShort(value)
      }
  }

  object Response {
    val size: Int = Request.size
  }

  override type RES = Response

  private object InitialResponseDecode extends ResponseDecodeState {
    override def decode(byteBuffer: ByteBuffer): Either[String, ResponseDecodeState] = {
      if (byteBuffer.remaining() < Request.size) return Left(s"The remaining size: ${byteBuffer.remaining()} must be at least ${Request.size}")

      val address = java.lang.Short.toUnsignedInt(byteBuffer.getShort)
      val value   = byteBuffer.getShort

      Right(ResponseFinalState(Response(address, value)))
    }

    override def toRes: Either[String, Response] = Left("Can't convert initial state into Response")

  }

  override def initialResponseDecodeState: ResponseDecodeState = InitialResponseDecode

  override def validateResponse(response: Response): Either[String, Response] =
    for {
      _ <- AddressValidator.validate(response.address)
    } yield response
}
