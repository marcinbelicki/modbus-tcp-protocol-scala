package pl.belicki.modbus.models

import pl.belicki.modbus.models.function.ModbusFunction

case class ModbusTCPADU(
    header: MBAPHeader,
    functionCode: ModbusFunction,
    data: Nothing
)
