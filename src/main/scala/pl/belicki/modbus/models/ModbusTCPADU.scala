package pl.belicki.modbus.models

case class ModbusTCPADU(
    header: MBAPHeader,
    functionCode: FunctionCode,
    data: Nothing
)
