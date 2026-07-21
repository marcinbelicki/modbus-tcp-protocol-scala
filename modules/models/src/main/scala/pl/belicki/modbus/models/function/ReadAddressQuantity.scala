package pl.belicki.modbus.models.function

import pl.belicki.modbus.models.ExceptionCode

import java.nio.ByteBuffer

trait ReadAddressQuantity {
  this: ModbusFunction =>

  def toRequest(address: Int, quantity: Int): REQ

  private object Initial extends DecodeState {
    override def decode(byteBuffer: ByteBuffer): Either[Error, DecodeState] = {
      if (byteBuffer.remaining() != 4) return ExceptionCode.ILLEGAL_DATA_VALUE
      val address  = java.lang.Short.toUnsignedInt(byteBuffer.getShort)
      val quantity = java.lang.Short.toUnsignedInt(byteBuffer.getShort)

      if (!validateQuantity(quantity)) return ExceptionCode.ILLEGAL_DATA_VALUE

      Right(FinalState(toRequest(address, quantity)))
    }

    override def toReq: Either[Error, REQ] = ExceptionCode.ILLEGAL_DATA_VALUE
  }

  override def initialDecodeState: DecodeState = Initial

  def validateQuantity(quantity: Int): Boolean

  protected def getAddress(request: REQ): Int
  protected def getQuantity(request: REQ): Int

  override def validateRequest(request: REQ): Either[String, REQ] = {
    val address       = getAddress(request)
    lazy val quantity = getQuantity(request)

    if (address < 0 || address > 0xffff)
      return Left(s"The address of the request: $address must be inside of the range <0x0000;0xffff>")

    if (!validateQuantity(quantity))
      return Left(s"The quantity of the request: $quantity is outside of accepted range: <0x0001;0x07d0>")

    Right(request)
  }

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
