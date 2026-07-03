package pl.belicki.modbus.models

trait CodedEnum[A] {
  def getCode: A
}
