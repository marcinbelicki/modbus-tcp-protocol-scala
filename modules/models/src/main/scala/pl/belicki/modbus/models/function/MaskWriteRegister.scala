package pl.belicki.modbus.models.function

import pl.belicki.modbus.models.ExceptionCode
import pl.belicki.modbus.models.validator.RangeValidator

import java.nio.ByteBuffer

object MaskWriteRegister extends ModbusFunction(0x16) {

  case class Request(
      address: Int,
      andMask: Short,
      orMask: Short
  ) extends super.Request {
    override def size: Int = Request.size

    override def encode(byteBuffer: ByteBuffer): Either[String, ByteBuffer] = for {
      _ <- validateRequest(this)
    } yield {
      byteBuffer.putShort(address.toShort)
      byteBuffer.putShort(andMask)
      byteBuffer.putShort(orMask)
    }

  }

  object Request {
    lazy val size: Int =
      java.lang.Short.BYTES * 3
  }

  type REQ = Request

  private object Initial extends RequestDecodeState {
    override def decode(byteBuffer: ByteBuffer): Either[ModbusError, RequestDecodeState] = {
      if (byteBuffer.remaining() != 6) return ExceptionCode.ILLEGAL_DATA_VALUE
      val address         = java.lang.Short.toUnsignedInt(byteBuffer.getShort)
      val andMask, orMask = byteBuffer.getShort

      Right(
        RequestFinalState(
          Request(
            address,
            andMask,
            orMask
          )
        )
      )
    }

    override def toReq: Either[ModbusError, Request] = ExceptionCode.ILLEGAL_DATA_VALUE
  }

  object AddressValidator extends RangeValidator(0x0000, 0xffff, "address")

  override def initialRequestDecodeState: RequestDecodeState = Initial

  override def validateRequest(request: Request): Either[String, Request] =
    for {
      _ <- AddressValidator.validate(request.address)
    } yield request

  case class Response(
      address: Int,
      andMask: Short,
      orMask: Short
  ) extends super.Response {

    override def encode(byteBuffer: ByteBuffer): Either[String, ByteBuffer] = for {
      _ <- validateResponse(this)
    } yield {
      byteBuffer.putShort(address.toShort)
      byteBuffer.putShort(andMask)
      byteBuffer.putShort(orMask)
    }

    override def size: Int = Response.size
  }

  object Response {
    val size: Int = Request.size
  }

  private object InitialResponseState extends ResponseDecodeState {
    override def decode(byteBuffer: ByteBuffer): Either[String, ResponseDecodeState] = {
      if (byteBuffer.remaining() != 6) return Left("There should be 6 remaining bytes")
      val address         = java.lang.Short.toUnsignedInt(byteBuffer.getShort)
      val andMask, orMask = byteBuffer.getShort

      Right(
        ResponseFinalState(
          Response(
            address,
            andMask,
            orMask
          )
        )
      )
    }

    override def toRes: Either[String, RES] = Left("Can't convert initial state into Response")
  }

  override type RES = Response

  override def initialResponseDecodeState: ResponseDecodeState = InitialResponseState

  override def validateResponse(response: Response): Either[String, Response] =
    for {
      _ <- AddressValidator.validate(response.address)
    } yield response
}
