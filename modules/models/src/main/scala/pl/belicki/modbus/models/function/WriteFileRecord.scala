package pl.belicki.modbus.models.function

object WriteFileRecord extends ModbusFunction(0x15) {

  case class SubRequest(
      fileNumber: Int,
      recordNumber: Int,
      recordLength: Int,
      recordData: Array[Byte]
  )

  case class Request(
      subRequests: List[SubRequest]
  ) extends super.Request

}
