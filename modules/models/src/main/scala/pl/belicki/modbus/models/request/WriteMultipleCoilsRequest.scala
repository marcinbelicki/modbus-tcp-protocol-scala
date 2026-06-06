package pl.belicki.modbus.models.request

case class WriteMultipleCoilsRequest(
    address: Int,
    quantity: Int,
    value: Array[Byte]
)
