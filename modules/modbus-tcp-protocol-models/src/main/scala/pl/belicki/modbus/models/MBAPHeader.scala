package pl.belicki.modbus.models

import pl.belicki.modbus.models.function.decoder.ModbusMessageDecoder
import pl.belicki.modbus.models.validator.RangeValidator

import java.nio.ByteBuffer

case class MBAPHeader(
    transactionIdentifier: Short,
    protocolIdentifier: Short,
    unitIdentifier: Byte
) {
  def encode(length: Short, byteBuffer: ByteBuffer): ByteBuffer = {
    byteBuffer.putShort(transactionIdentifier)
    byteBuffer.putShort(protocolIdentifier)
    byteBuffer.putShort(length)
    byteBuffer.put(unitIdentifier)
  }

}

object MBAPHeader {
  val SIZE: Int = java.lang.Short.BYTES * 3 + java.lang.Byte.BYTES
  object LengthValidator extends RangeValidator(0x0000, 0xffff, "length")

  def decode[E](decoder: ModbusMessageDecoder[E, _])(data: ByteBuffer): Either[E, MBAPHeader] = {

    if (data.remaining() < SIZE) return Left(decoder.dataTooShort)

    val transactionIdentifier = data.getShort()
    val protocolIdentifier    = data.getShort()
    val length                = java.lang.Short.toUnsignedInt(data.getShort())

    if (length != data.remaining()) return Left(decoder.lengthIncorrect)

    val unitIdentifier = data.get()

    Right(
      MBAPHeader(
        transactionIdentifier,
        protocolIdentifier,
        unitIdentifier
      )
    )
  }
}
