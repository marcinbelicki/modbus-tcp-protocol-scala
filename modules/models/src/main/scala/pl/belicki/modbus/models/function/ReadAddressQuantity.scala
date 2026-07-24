package pl.belicki.modbus.models.function

import pl.belicki.modbus.models.ExceptionCode
import pl.belicki.modbus.models.validator.RangeValidator

import java.nio.ByteBuffer

trait ReadAddressQuantity {
  this: ModbusFunction =>

  def toRequest(address: Int, quantity: Int): REQ

  private object Initial extends DecodeState {
    override def decode(byteBuffer: ByteBuffer): Either[Error, DecodeState] = {
      if (byteBuffer.remaining() != 4) return ExceptionCode.ILLEGAL_DATA_VALUE
      val address  = java.lang.Short.toUnsignedInt(byteBuffer.getShort)
      val quantity = java.lang.Short.toUnsignedInt(byteBuffer.getShort)

      if (!quantityValidator.validateBool(quantity)) return ExceptionCode.ILLEGAL_DATA_VALUE

      Right(FinalState(toRequest(address, quantity)))
    }

    override def toReq: Either[Error, REQ] = ExceptionCode.ILLEGAL_DATA_VALUE
  }

  override def initialDecodeState: DecodeState = Initial

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

}

object ReadAddressQuantity {
  lazy val requestSize: Int = java.lang.Short.BYTES * 2
}
