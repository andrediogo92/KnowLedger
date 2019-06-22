import hashlib

m = [hashlib.sha256(), hashlib.sha512(), hashlib.blake2b(), hashlib.blake2s(), hashlib.sha3_256(), hashlib.sha3_512()]
input = b"testByteArray"
for n in m:
    n.update(input)
    print(n.digest().hex().upper())
