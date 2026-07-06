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
}
