package pl.belicki.modbus.models.function

import pl.belicki.modbus.models.ExceptionCode
import pl.belicki.modbus.models.validator.RangeValidator

import java.nio.ByteBuffer

object WriteSingleCoil extends ModbusFunction(0x05) {
  import Initial.valueMap

  case class Request(
      address: Int,
      value: Boolean
  ) extends super.Request {
    override def size: Int = Request.size

    override def encode(byteBuffer: ByteBuffer): Either[String, ByteBuffer] =
      for {
        _ <- validateRequest(this)
      } yield {
        byteBuffer.putShort(address.toShort)
        byteBuffer.putShort(shortByBoolean(value))
      }

  }

  object Request {
    val size: Int = java.lang.Short.BYTES * 2

  }

  type REQ = Request

  private object Initial extends RequestDecodeState {
    val valueMap: Map[Short, Boolean] = shortByBoolean.map(_.swap)

    override def decode(byteBuffer: ByteBuffer): Either[ModbusError, RequestDecodeState] = {
      if (byteBuffer.remaining() < 4) return ExceptionCode.ILLEGAL_DATA_VALUE
      val address = java.lang.Short.toUnsignedInt(byteBuffer.getShort)
      for {
        value <- valueMap.get(byteBuffer.getShort).toRight(ExceptionCode.ILLEGAL_DATA_VALUE)
      } yield RequestFinalState(Request(address, value))
    }

    override def toReq: Either[ModbusError, Request] = ExceptionCode.ILLEGAL_DATA_VALUE
  }

  object AddressValidator extends RangeValidator(0x0000, 0xffff, "address")

  private val shortByBoolean = Map(
    true -> 0xff00.toShort,
    false -> 0x0000.toShort
  )

  override def initialRequestDecodeState: RequestDecodeState = Initial

  override def validateRequest(request: Request): Either[String, Request] =
    for {
      _ <- AddressValidator.validate(request.address)
    } yield request

  case class Response(
      address: Int,
      value: Boolean
  ) extends super.Response {
    override def size: Int = Response.size

    override def encode(byteBuffer: ByteBuffer): Either[String, ByteBuffer] =
      for {
        _ <- validateResponse(this)
      } yield {
        byteBuffer.putShort(address.toShort)
        byteBuffer.putShort(shortByBoolean(value))
      }

  }

  object Response {
    val size: Int = Request.size
  }

  private object InitialResponseDecodeState extends ResponseDecodeState {

    override def decode(byteBuffer: ByteBuffer): Either[String, ResponseDecodeState] = {
      if (byteBuffer.remaining() != Response.size)
        return Left(s"The number of remaining bytes: ${byteBuffer.remaining()} must be equal to: ${Response.size} ")
      val address = java.lang.Short.toUnsignedInt(byteBuffer.getShort)

      for {
        value <- valueMap.get(byteBuffer.getShort).toRight("The value of these bytes is expected to be either 0x0000 or 0xff00")
      } yield ResponseFinalState(Response(address, value))
    }

    override def toRes: Either[String, Response] = Left("Can't convert initial state into Response")
  }

  override type RES = Response

  override def initialResponseDecodeState: ResponseDecodeState = InitialResponseDecodeState

  override def validateResponse(response: Response): Either[String, Response] = for {
    _ <- AddressValidator.validate(response.address)
  } yield response
}
