package pl.belicki.modbus.models

import pl.belicki.modbus.models.function.decoder.ModbusMessageDecoder

import java.nio.ByteBuffer

case class MBAPHeader(
    transactionIdentifier: Int,
    protocolIdentifier: Int,
    length: Int,
    unitIdentifier: Short
)

object MBAPHeader {
  def decode[E,M](decoder: ModbusMessageDecoder[E, M])(byteBuffer: ByteBuffer) = {

    if
  }
}
