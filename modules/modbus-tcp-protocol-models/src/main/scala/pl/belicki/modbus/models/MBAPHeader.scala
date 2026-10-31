package pl.belicki.modbus.models

case class MBAPHeader(
    transactionIdentifier: Int,
    protocolIdentifier: Int,
    length: Int,
    unitIdentifier: Short
)
