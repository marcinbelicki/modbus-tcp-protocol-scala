package pl.belicki.modbus.models.function

import pl.belicki.modbus.models.validator.RangeValidator

import java.nio.ByteBuffer

object ReadCoils extends ModbusFunction(0x01) with ReadBits {

  case class Request(
      address: Int,
      quantity: Int
  ) extends super.Request {
    override def size: Int = ReadAddressQuantity.requestSize

    override def encode(byteBuffer: ByteBuffer): Either[String, ByteBuffer] = encodeRequest(byteBuffer, this)
  }

  type REQ = Request

  override def toRequest(address: Int, quantity: Int): Request = Request(address, quantity)

  override protected def getAddress(request: Request): Int = request.address

  override protected def getQuantity(request: Request): Int = request.quantity

  case class Response(
      coilStatus: Array[Byte]
  ) extends super.Request {
    override def size: Int = coilStatus.length + java.lang.Byte.BYTES

    override def encode(byteBuffer: ByteBuffer): Either[String, ByteBuffer] = for {
      _ <- validateResponse(this)
    } yield {
      byteBuffer.put(coilStatus.length.toByte)
      byteBuffer.put(coilStatus)
    }
  }

  type RES = Response

  private val coilStatusSizeValidator = new RangeValidator(0x00, 0xff, "size", "02X")

  private object InitialResponseState extends ResponseDecodeState {
    override def decode(byteBuffer: ByteBuffer): Either[String, ResponseDecodeState] = {
      if (byteBuffer.remaining() < 1) return Left("Not enough bytes.")
      val byteCount = java.lang.Byte.toUnsignedInt(byteBuffer.get)

      if (byteBuffer.remaining() != byteCount) return Left("Not enough bytes to read coil status.")
      val coilStatus = new Array[Byte](byteCount)
      byteBuffer.get(coilStatus)

      Right(ResponseFinalState(Response(coilStatus)))
    }

    override def toRes: Either[String, Response] = Left("Can't convert initial state into Response")
  }

  override def initialResponseDecodeState: ResponseDecodeState = InitialResponseState

  override def validateResponse(response: Response): Either[String, Response] = for {
    _ <- coilStatusSizeValidator.validate(response.coilStatus.length)
  } yield response
}
