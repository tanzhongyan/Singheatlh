#!/usr/bin/env python3
import jwt
import os
from dotenv import load_dotenv

load_dotenv()

JWT_SECRET = os.getenv('JWT_SECRET')
SERVICE_KEY = os.getenv('SERVICE_ROLE_KEY')
ANON_KEY = os.getenv('ANON_KEY')

print("🔐 Verifying tokens against your JWT_SECRET...")

# Verify SERVICE_ROLE_KEY
try:
    decoded_service = jwt.decode(SERVICE_KEY, JWT_SECRET, algorithms=["HS256"])
    print("✅ SERVICE_ROLE_KEY is VALID")
    print(f"   Role: {decoded_service['role']}")
    print(f"   Issuer: {decoded_service['iss']}")
except Exception as e:
    print(f"❌ SERVICE_ROLE_KEY is INVALID: {e}")

# Verify ANON_KEY  
try:
    decoded_anon = jwt.decode(ANON_KEY, JWT_SECRET, algorithms=["HS256"])
    print("✅ ANON_KEY is VALID")
    print(f"   Role: {decoded_anon['role']}")
    print(f"   Issuer: {decoded_anon['iss']}")
except Exception as e:
    print(f"❌ ANON_KEY is INVALID: {e}")