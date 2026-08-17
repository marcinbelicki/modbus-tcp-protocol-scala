package pl.belicki.modbus.models.function

import pl.belicki.modbus.models.ExceptionCode
import pl.belicki.modbus.models.validator.RangeValidator

import java.nio.ByteBuffer

trait ReadAddressQuantity {
  this: ModbusFunction =>

  def toRequest(address: Int, quantity: Int): REQ

  private object Initial extends RequestDecodeState {
    override def decode(byteBuffer: ByteBuffer): Either[ModbusError, RequestDecodeState] = {
      if (byteBuffer.remaining() != 4) return ExceptionCode.ILLEGAL_DATA_VALUE
      val address  = java.lang.Short.toUnsignedInt(byteBuffer.getShort)
      val quantity = java.lang.Short.toUnsignedInt(byteBuffer.getShort)

      if (!quantityValidator.validateBool(quantity)) return ExceptionCode.ILLEGAL_DATA_VALUE

      Right(RequestFinalState(toRequest(address, quantity)))
    }

    override def toReq: Either[ModbusError, REQ] = ExceptionCode.ILLEGAL_DATA_VALUE
  }

  override def initialRequestDecodeState: RequestDecodeState = Initial

  protected def getAddress(request: REQ): Int
  protected def getQuantity(request: REQ): Int

  object AddressValidator extends RangeValidator(0x0000, 0xffff, "address")
  def quantityValidator: RangeValidator

  override def validateRequest(request: REQ): Either[String, REQ] =
    for {
      _ <- AddressValidator.validate(getAddress(request))
      _ <- quantityValidator.validate(getQuantity(request))
    } yield request

  protected def encodeRequest(byteBuffer: ByteBuffer, request: REQ): Either[String, ByteBuffer] =
    for {
      _ <- validateRequest(request)
    } yield {
      byteBuffer.putShort(getAddress(request).toShort)
      byteBuffer.putShort(getQuantity(request).toShort)
    }

  def toResponse(bytes: Array[Byte]): RES
  def getByteCount(res: RES): Int
  def validateByteCount(byteCount: Int): Either[String, Unit]

  private val byteCountLengthValidator = new RangeValidator(0x00, 0xff, "size", "02X")

  private object InitialResponseState extends ResponseDecodeState {
    override def decode(byteBuffer: ByteBuffer): Either[String, ResponseDecodeState] = {
      for {
        _ <- Either.cond(byteBuffer.remaining() > 0, (), "Not enough bytes.")
        byteCount = java.lang.Byte.toUnsignedInt(byteBuffer.get)
        _ <- Either.cond(byteBuffer.remaining() == byteCount, (), "Not enough bytes to read coil status.")
        _ <- validateByteCount(byteCount)
        bytes = new Array[Byte](byteCount)
      } yield {
        byteBuffer.get(bytes)
        ResponseFinalState(toResponse(bytes))
      }

    }

    override def toRes: Either[String, RES] = Left("Can't convert initial state into Response")
  }

  override def initialResponseDecodeState: ResponseDecodeState = InitialResponseState

  override def validateResponse(response: RES): Either[String, Response] = for {
    _ <- byteCountLengthValidator.validate(getByteCount(response))
  } yield response

}

object ReadAddressQuantity {
  lazy val requestSize: Int = java.lang.Short.BYTES * 2
}
