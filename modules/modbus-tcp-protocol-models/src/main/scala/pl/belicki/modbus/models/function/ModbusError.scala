package pl.belicki.modbus.models.function

import pl.belicki.modbus.models.ExceptionCode

import java.nio.ByteBuffer

case class ModbusError(exceptionCode: ExceptionCode, functionCode: Byte) extends Message {

  override val size: Int = java.lang.Byte.BYTES * 2

  override def encode(byteBuffer: ByteBuffer): Either[String, ByteBuffer] = Right(byteBuffer.put(exceptionCode.getCode))

  override def putCode(byteBuffer: ByteBuffer): Unit = byteBuffer.put(functionCode)

  override def fullSize: Int = size
}
