package pl.belicki.modbus.models.function

import pl.belicki.modbus.models.function.Message.MessageSizeValidator
import pl.belicki.modbus.models.validator.RangeValidator

import java.nio.ByteBuffer

trait Message {
  def size: Int
  def encode(byteBuffer: ByteBuffer): Either[String, ByteBuffer]
  def putCode(byteBuffer: ByteBuffer): Unit

  def fullSize: Int

  def toByteBuffer: Either[String, ByteBuffer] = {
    for {
      _ <- MessageSizeValidator.validate(fullSize)
      byteBuffer = ByteBuffer.allocate(fullSize)
      _          = putCode(byteBuffer)
      byteBuffer <- encode(byteBuffer)
    } yield byteBuffer
  }
}

object Message {

  object MessageSizeValidator extends RangeValidator(0x0000, 0x00fd, "size")
}
