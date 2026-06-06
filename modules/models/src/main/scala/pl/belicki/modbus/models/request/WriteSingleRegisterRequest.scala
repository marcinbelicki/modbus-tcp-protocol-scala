package pl.belicki.modbus.models.request

import pl.belicki.modbus.models.{FunctionCode, PublicFunctionCode}

case class WriteSingleRegisterRequest(
    address: Int,
    value: Short
) extends ModbusRequest {

  override def getCode: FunctionCode = PublicFunctionCode.WriteSingleRegister
}
