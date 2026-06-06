package pl.belicki.modbus.models.request

import pl.belicki.modbus.models.{FunctionCode, PublicFunctionCode}

case class WriteSingleCoilRequest(
    address: Int,
    value: Boolean
) extends ModbusRequest {
  override def getCode: FunctionCode = PublicFunctionCode.WriteSingleCoil
}
