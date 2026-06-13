package pl.belicki.modbus.models.function

object ReadInputRegisters extends ModbusFunction(0x04) with ReadRegisters {

  case class Request(
      address: Int,
      quantity: Int
  ) extends super.Request

  override type REQ = Request

  override def toRequest(address: Int, quantity: Int): Request = Request(address, quantity)
}
