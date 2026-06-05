package pl.belicki.modbus.models

case class ModbusTCPADU(
                         header: MBAPHeader,
                         functionCode: PublicFunctionCode,
                         data: Nothing
)
