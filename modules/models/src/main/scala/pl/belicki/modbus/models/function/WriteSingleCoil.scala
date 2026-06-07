package pl.belicki.modbus.models.function

object WriteSingleCoil extends ModbusFunction(0x05) {

  case class Request(
      address: Int,
      value: Boolean
  ) extends super.Request

}
