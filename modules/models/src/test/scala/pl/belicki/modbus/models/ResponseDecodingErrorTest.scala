package pl.belicki.modbus.models

import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatest.prop.TableDrivenPropertyChecks.forAll
import org.scalatest.prop.Tables.Table
import org.scalatest.wordspec.AnyWordSpecLike
import pl.belicki.modbus.models.function.eit.EncapsulatedInterfaceTransport.ReadDeviceIdentification.ObjectInfo
import pl.belicki.modbus.models.function.eit.{ConformityLevel, DefinedObjectId, EncapsulatedInterfaceTransport, ReadDeviceIdCode}
import pl.belicki.modbus.models.function.{
  MaskWriteRegister, ModbusResponseDecoder, ReadCoils, ReadDiscreteInputs, ReadFIFOQueue, ReadFileRecord, ReadHoldingRegisters, ReadInputRegisters,
  ReadWriteMultipleRegisters, WriteFileRecord, WriteMultipleCoils, WriteMultipleRegisters, WriteSingleCoil, WriteSingleRegister
}

class ResponseDecodingErrorTest extends AnyWordSpecLike {

  private val hexWithRequests =
    Table(
      ("hex",            "expectedError"),
      ("01 02 CD 6B 05", "The number of the remaining bytes (3) must be equal to the expected byte count (2). | position: 2")
    )

  private val decoder = ModbusResponseDecoder.ALL_FUNCTIONS
  "ResponseDecoder" must {
    "properly result with decoding error" in {
      forAll(hexWithRequests) {
        (hex, expectedError) =>
          val Left(message) = decoder.decodeHexResponse(hex): @unchecked

          message shouldBe expectedError

      }
    }

  }

}
