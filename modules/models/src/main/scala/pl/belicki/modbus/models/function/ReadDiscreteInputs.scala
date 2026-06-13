package pl.belicki.modbus.models.function

object ReadDiscreteInputs extends ModbusFunction(0x02) with ReadBits {

  case class Request(
      address: Int,
      quantity: Int
  ) extends super.Request

  override type REQ = Request

  override def toRequest(address: Int, quantity: Int): Request = Request(address, quantity)
}
