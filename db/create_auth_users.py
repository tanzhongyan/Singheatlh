#!/usr/bin/env python3
import os
import csv
import requests
import json
from dotenv import load_dotenv

# Load environment variables from .env file
load_dotenv()

# Supabase configuration from .env
SUPABASE_URL = os.getenv('API_EXTERNAL_URL')
SERVICE_KEY = os.getenv('SERVICE_ROLE_KEY')
POSTGRES_PASSWORD = os.getenv('POSTGRES_PASSWORD')
JWT_SECRET = os.getenv('JWT_SECRET')
ANON_KEY = os.getenv('ANON_KEY')
MOCK_USER_PASSWORD = os.getenv('MOCK_USER_PASSWORD')
DATA_DIR="sample-data-mini"

print(f"🔑 Using Service Key: {SERVICE_KEY}")
print(f"🗄️ Postgres Password: {POSTGRES_PASSWORD}")
print(f"🛡️ JWT Secret: {JWT_SECRET}")
print(f"🔐 Anon Key: {ANON_KEY}")
print(f"🔒 Mock User Password: {MOCK_USER_PASSWORD}")

def create_auth_users():
    users = []
    
    # Read user_profile.csv
    with open(f'{DATA_DIR}/user_profile.csv', 'r') as f:
        reader = csv.DictReader(f)
        for row in reader:
            users.append({
                'email': row['email'],
                'password': MOCK_USER_PASSWORD,  
                'user_metadata': {'name': row['name']},
                'email_confirm': True
            })
    
    print(f"🔑 Using Service Key: {SERVICE_KEY[:10]}...") if SERVICE_KEY else None
    print(f"📧 Creating {len(users)} auth users...")
    
    # Create users via Supabase Admin API
    for user in users:
        try:
            response = requests.post(
                f"{SUPABASE_URL}/auth/v1/admin/users",
               headers = {
                    'Authorization': f'Bearer {SERVICE_KEY}',
                    'apikey': SERVICE_KEY,
                    'Content-Type': 'application/json'
               },   
                json=user,
                timeout=30
            )
            
            if response.status_code == 200:
                user_data = response.json()
                # print(f"✅ Created user: {user['email']} (ID: {user_data['id'][:8]}...)")
            else:
                print(f"❌ Failed to create {user['email']}: {response.status_code} - {response.text}")
                
        except requests.exceptions.RequestException as e:
            print(f"❌ Network error for {user['email']}: {e}")

def verify_env_vars():
    """Check if all required environment variables are set"""
    required_vars = ['SERVICE_ROLE_KEY', 'POSTGRES_PASSWORD', 'JWT_SECRET', 'ANON_KEY']
    missing_vars = [var for var in required_vars if not os.getenv(var)]
    
    if missing_vars:
        print(f"❌ Missing environment variables: {', '.join(missing_vars)}")
        print("💡 Make sure your .env file is in the same folder as this script")
        return False
    return True

if __name__ == "__main__":
    if not verify_env_vars():
        exit(1)
    
    create_auth_users()