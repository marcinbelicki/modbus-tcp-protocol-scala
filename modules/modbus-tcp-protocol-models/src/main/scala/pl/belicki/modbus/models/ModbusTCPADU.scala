package pl.belicki.modbus.models

import pl.belicki.modbus.models.function.ModbusFunction
import pl.belicki.modbus.models.function.decoder.ModbusMessageDecoder

import java.nio.ByteBuffer

case class ModbusTCPADU[M <: ModbusFunction#Message](
    header: MBAPHeader,
    message: M
)

object ModbusTCPADU {
  val MAX_LENGTH: Int = 260

  def decode[E, M <: ModbusFunction#Message](decoder: ModbusMessageDecoder[E, M])(data: ByteBuffer): Either[E, ModbusTCPADU[M]] = {
    if (data.remaining() > MAX_LENGTH) return Left(decoder.dataTooLong)
    for {
      header  <- MBAPHeader.decode(decoder)(data)
      message <- decoder.decode(data)
    } yield ModbusTCPADU(header, message)
  }
}
