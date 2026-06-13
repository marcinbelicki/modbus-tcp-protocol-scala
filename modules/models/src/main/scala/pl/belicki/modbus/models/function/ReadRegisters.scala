package pl.belicki.modbus.models.function

trait ReadRegisters extends ReadAddressQuantity {
  this: ModbusFunction =>

  def validateQuantity(quantity: Int): Boolean = quantity < 125 && quantity > 0

}
