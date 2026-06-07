package pl.belicki.modbus.models.function

object ReadHoldingRegisters extends ModbusFunction(0x03) {

  case class Request(
      address: Int,
      quantity: Int
  ) extends super.Request

}
