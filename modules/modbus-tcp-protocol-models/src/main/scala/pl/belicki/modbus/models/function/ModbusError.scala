package pl.belicki.modbus.models.function

import pl.belicki.modbus.models.ExceptionCode

case class ModbusError(exceptionCode: ExceptionCode, functionCode: Byte)
