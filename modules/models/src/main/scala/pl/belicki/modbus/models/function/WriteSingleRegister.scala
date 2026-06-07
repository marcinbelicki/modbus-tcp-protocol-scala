package pl.belicki.modbus.models.function

object WriteSingleRegister extends ModbusFunction(0x06) {

  case class Request(
      address: Int,
      value: Short
  ) extends super.Request

}
