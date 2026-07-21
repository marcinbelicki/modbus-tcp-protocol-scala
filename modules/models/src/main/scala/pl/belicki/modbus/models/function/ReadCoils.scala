package pl.belicki.modbus.models.function

import java.nio.ByteBuffer

object ReadCoils extends ModbusFunction(0x01) with ReadBits {

  case class Request(
      address: Int,
      quantity: Int
  ) extends super.Request {
    override def size: Int = ReadAddressQuantity.requestSize

    override def encode(byteBuffer: ByteBuffer): Either[String, ByteBuffer] = encodeRequest(byteBuffer, this)
  }

  type REQ = Request

  override def toRequest(address: Int, quantity: Int): Request = Request(address, quantity)

  override protected def getAddress(request: Request): Int = request.address

  override protected def getQuantity(request: Request): Int = request.quantity
}
