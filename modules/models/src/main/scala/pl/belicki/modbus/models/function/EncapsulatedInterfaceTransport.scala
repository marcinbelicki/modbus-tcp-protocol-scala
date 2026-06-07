package pl.belicki.modbus.models.function

object EncapsulatedInterfaceTransport extends ModbusFunction(0x2b) {

  abstract class SubFunction(_code: Int) {

    val code: Byte = _code.toByte

    abstract class Request extends EncapsulatedInterfaceTransport.Request {
      val subFunction: SubFunction = SubFunction.this
    }

  }

  object ReadDeviceIdentification extends SubFunction(0x0e) {

    case class Request(
        deviceIdCode: Byte,
        objectId: Short
    ) extends super.Request

  }

  object CANopenGeneralReference extends SubFunction(0x0d) {

    case class Request() extends super.Request

  }
}
