package pl.belicki.modbus.models.function

object ReadFileRecord extends ModbusFunction(0x14) {

  case class SubRequest(
      fileNumber: Int,
      recordNumber: Int,
      recordLength: Int
  )

  case class Request(
      subRequests: List[SubRequest]
  ) extends super.Request

}
