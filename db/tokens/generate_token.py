#!/usr/bin/env python3
import jwt
import datetime

# Use the EXACT JWT secret from your .env file
JWT_SECRET = "your-super-secret-jwt-token-with-at-least-32-characters-long"

def generate_tokens():
    # Service Role Token
    service_payload = {
        "role": "service_role",
        "iss": "supabase",
        "iat": datetime.datetime.utcnow(),
        "exp": datetime.datetime.utcnow() + datetime.timedelta(days=365)
    }
    service_token = jwt.encode(service_payload, JWT_SECRET, algorithm="HS256")
    
    # Anon Token  
    anon_payload = {
        "role": "anon",
        "iss": "supabase", 
        "iat": datetime.datetime.utcnow(),
        "exp": datetime.datetime.utcnow() + datetime.timedelta(days=365)
    }
    anon_token = jwt.encode(anon_payload, JWT_SECRET, algorithm="HS256")
    
    print("🔄 Replace these tokens in your .env file:")
    print("")
    print(f"SERVICE_ROLE_KEY={service_token}")
    print("")
    print(f"ANON_KEY={anon_token}")
    print("")
    print("✅ Then restart your containers: docker compose down && docker compose up -d")

if __name__ == "__main__":
    generate_tokens()