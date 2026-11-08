package pl.belicki.modbus.models

import pl.belicki.modbus.models.function.ModbusFunction
import pl.belicki.modbus.models.function.decoder.ModbusMessageDecoder

import java.nio.ByteBuffer

case class ModbusTCPADU[M <: ModbusFunction#Message](
    header: MBAPHeader,
    message: M
)

object ModbusTCPADU {
//  def decode[E, M <: ModbusFunction#Message](decoder: ModbusMessageDecoder[E, M])(data: ByteBuffer):  = {
//    for
//  }
}
