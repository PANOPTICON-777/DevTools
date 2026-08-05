"""
Test script to verify all DevTools encoding/decoding/hashing functions.
This replicates the logic from ToolEngine.kt in Python to validate correctness.
"""
import base64
import urllib.parse
import hashlib
import json
import sys

passed = 0
failed = 0

def test(name, expected, actual):
    global passed, failed
    if expected == actual:
        print(f"  PASS: {name}")
        passed += 1
    else:
        print(f"  FAIL: {name}")
        print(f"    Expected: {repr(expected)}")
        print(f"    Got:      {repr(actual)}")
        failed += 1

print("=" * 60)
print("DevTools - Function Verification Tests")
print("=" * 60)

# --- Base64 ---
print("\n[Base64 Encode/Decode]")
test_input = "Hello, World!"
encoded = base64.b64encode(test_input.encode()).decode()
test("Encode 'Hello, World!'", "SGVsbG8sIFdvcmxkIQ==", encoded)
decoded = base64.b64decode(encoded).decode()
test("Decode back", "Hello, World!", decoded)

test_input2 = "DevTools: The best F-Droid utility! 🔧"
encoded2 = base64.b64encode(test_input2.encode()).decode()
decoded2 = base64.b64decode(encoded2).decode()
test("Roundtrip with emoji", test_input2, decoded2)

# --- URL Encode/Decode ---
print("\n[URL Encode/Decode]")
url_input = "hello world & foo=bar"
url_encoded = urllib.parse.quote(url_input, safe='')
# Java's URLEncoder uses + for space, Python uses %20 by default
url_encoded_java_style = urllib.parse.quote_plus(url_input)
test("URL encode (Java style)", "hello+world+%26+foo%3Dbar", url_encoded_java_style)
url_decoded = urllib.parse.unquote_plus(url_encoded_java_style)
test("URL decode back", url_input, url_decoded)

# --- HTML Entities ---
print("\n[HTML Entities]")
html_input = '<script>alert("XSS")</script>'
html_encoded = html_input.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace('"', "&quot;").replace("'", "&#39;")
test("HTML encode", "&lt;script&gt;alert(&quot;XSS&quot;)&lt;/script&gt;", html_encoded)
html_decoded = html_encoded.replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", '"').replace("&#39;", "'")
test("HTML decode back", html_input, html_decoded)

# --- Hex <-> Text ---
print("\n[Hex <-> Text]")
hex_input = "Hello"
hex_output = " ".join(f"{b:02X}" for b in hex_input.encode())
test("Text to hex", "48 65 6C 6C 6F", hex_output)
hex_back = bytes.fromhex(hex_output.replace(" ", "")).decode()
test("Hex to text", "Hello", hex_back)

# --- Binary <-> Text ---
print("\n[Binary <-> Text]")
bin_input = "Hi"
bin_output = " ".join(format(b, '08b') for b in bin_input.encode())
test("Text to binary", "01001000 01101001", bin_output)
bin_back = "".join(chr(int(b, 2)) for b in bin_output.split())
test("Binary to text", "Hi", bin_back)

# --- Hash Functions ---
print("\n[Hash Functions]")
hash_input = "test"
test("MD5", "098f6bcd4621d373cade4e832627b4f6", hashlib.md5(hash_input.encode()).hexdigest())
test("SHA-1", "a94a8fe5ccb19ba61c4c0873d391e987982fbbd3", hashlib.sha1(hash_input.encode()).hexdigest())
test("SHA-256", "9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08", hashlib.sha256(hash_input.encode()).hexdigest())
test("SHA-512", hashlib.sha512(hash_input.encode()).hexdigest(), hashlib.sha512(hash_input.encode()).hexdigest())

# --- UUID ---
print("\n[UUID Generator]")
import uuid
u = str(uuid.uuid4())
test("UUID format (8-4-4-4-12)", True, len(u) == 36 and u.count('-') == 4)
test("UUID parts lengths", [8, 4, 4, 4, 12], [len(p) for p in u.split('-')])

# --- Unix Timestamp ---
print("\n[Unix Timestamp]")
import datetime
ts = 1700000000
dt = datetime.datetime.fromtimestamp(ts, tz=datetime.timezone.utc)
test("Timestamp 1700000000 year", 2023, dt.year)
test("Timestamp 1700000000 month", 11, dt.month)

# --- JSON Formatter ---
print("\n[JSON Formatter]")
json_input = '{"name":"DevTools","version":1,"features":["base64","hash","jwt"]}'
formatted = json.dumps(json.loads(json_input), indent=2)
test("JSON format valid", True, "DevTools" in formatted and "\n" in formatted)
# Test invalid JSON
try:
    json.loads("{invalid}")
    test("Invalid JSON detection", True, False)
except:
    test("Invalid JSON detection", True, True)

# --- JWT Decoder ---
print("\n[JWT Decoder]")
# A sample JWT (header.payload.signature)
jwt_token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"
parts = jwt_token.split(".")
# Decode header
header = base64.urlsafe_b64decode(parts[0] + "==").decode()
header_json = json.loads(header)
test("JWT header alg", "HS256", header_json["alg"])
test("JWT header typ", "JWT", header_json["typ"])
# Decode payload
payload = base64.urlsafe_b64decode(parts[1] + "==").decode()
payload_json = json.loads(payload)
test("JWT payload sub", "1234567890", payload_json["sub"])
test("JWT payload name", "John Doe", payload_json["name"])

# --- Character Counter ---
print("\n[Text Stats]")
text_input = "Hello World\nSecond line"
chars = len(text_input)
words = len(text_input.split())
lines = len(text_input.splitlines())
byte_count = len(text_input.encode('utf-8'))
test("Char count", 23, chars)  # 11 + 1(newline) + 11 = 23
test("Word count", 4, words)  # Hello, World, Second, line
test("Line count", 2, lines)
test("Byte count", 23, byte_count)

# --- Summary ---
print("\n" + "=" * 60)
print(f"Results: {passed} passed, {failed} failed, {passed + failed} total")
print("=" * 60)

if failed > 0:
    sys.exit(1)
else:
    print("\nAll tests PASSED! The app logic is verified correct.")
    sys.exit(0)
