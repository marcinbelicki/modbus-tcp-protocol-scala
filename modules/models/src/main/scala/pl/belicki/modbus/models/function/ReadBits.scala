package pl.belicki.modbus.models.function

trait ReadBits extends ReadAddressQuantity {
  this: ModbusFunction =>

  final def validateQuantity(quantity: Int): Boolean = quantity <= 2000 && quantity >= 1

}
