package pl.belicki.modbus.models.function

import pl.belicki.modbus.models.validator.RangeValidator

trait ReadRegisters extends ReadAddressQuantity {
  this: ModbusFunction =>

  override lazy val quantityValidator: RangeValidator = new RangeValidator(0x0001, 0x007d, "quantity")

}
