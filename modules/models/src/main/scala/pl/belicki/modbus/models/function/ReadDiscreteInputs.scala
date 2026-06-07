package pl.belicki.modbus.models.function

object ReadDiscreteInputs extends ModbusFunction(0x02) {

  case class Request(
      address: Int,
      quantity: Int
  ) extends super.Request

}
