package pl.belicki.modbus.models

import pl.belicki.modbus.models.function.decoder.ModbusMessageDecoder

import java.nio.ByteBuffer

case class MBAPHeader(
    transactionIdentifier: Short,
    protocolIdentifier: Short,
    length: Int,
    unitIdentifier: Byte
)

object MBAPHeader {
  val SIZE = java.lang.Short.BYTES * 3 + java.lang.Byte.BYTES

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
        length,
        unitIdentifier
      )
    )

  }
}
