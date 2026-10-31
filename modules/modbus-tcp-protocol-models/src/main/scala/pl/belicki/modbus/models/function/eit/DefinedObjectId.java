package pl.belicki.modbus.models.function.eit;

public enum DefinedObjectId implements ObjectId {
    VendorName(0x00),
    ProductCode(0x01),
    MajorMinorRevision(0x02),
    VendorUrl(0x03),
    ProductName(0x04),
    ModelName(0x05),
    UserApplicationName(0x06);

    private final byte code;

    DefinedObjectId(int _code) {
        this.code = (byte) _code;
    }

    public final byte getCode() {
        return this.code;
    }
}
