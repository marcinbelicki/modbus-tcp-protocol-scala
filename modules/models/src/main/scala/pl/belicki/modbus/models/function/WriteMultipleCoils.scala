package pl.belicki.modbus.models.function

object WriteMultipleCoils extends ModbusFunction(0x0f) {

  case class Request(
      address: Int,
      quantity: Int,
      value: Array[Byte]
  ) extends super.Request

}
