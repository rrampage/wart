
# From https://github.com/mohanson/leb128/tree/master
def encode_unsigned(i: int) -> bytearray:
    """Encode the int i using unsigned leb128 and return the encoded bytearray."""
    assert i >= 0
    r = []
    while True:
        byte = i & 0x7f
        i = i >> 7
        if i == 0:
            r.append(byte)
            return bytearray(r)
        r.append(0x80 | byte)


def decode_unsigned(b: bytearray) -> int:
    """Decode the unsigned leb128 encoded bytearray"""
    r = 0
    for i, e in enumerate(b):
        r = r + ((e & 0x7f) << (i * 7))
    return r


def encode_signed(i: int) -> bytearray:
    """Encode the int i using signed leb128 and return the encoded bytearray."""
    r = []
    while True:
        byte = i & 0x7f
        i = i >> 7
        if (i == 0 and byte & 0x40 == 0) or (i == -1 and byte & 0x40 != 0):
            r.append(byte)
            return bytearray(r)
        r.append(0x80 | byte)


def decode_signed(b: bytearray) -> int:
    """Decode the signed leb128 encoded bytearray"""
    r = 0
    for i, e in enumerate(b):
        r = r + ((e & 0x7f) << (i * 7))
    if e & 0x40 != 0:
        r |= - (1 << (i * 7) + 7)
    return r


if __name__ == '__main__':
    print(decode_signed(encode_signed(-123456)))
    print(encode_unsigned(2**31-1).hex())
    print(encode_signed(2**32-1).hex())
