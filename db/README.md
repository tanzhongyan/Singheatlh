# Supabase Docker

This is a minimal Docker Compose setup for self-hosting Supabase. Follow the steps [here](https://supabase.com/docs/guides/hosting/docker) to get started.

# Key generation (to be used when the JWT secret has changed)

Run the following to generate new service and anon keys

```bash
python -m pip install pyjwt # install required dependencies

# Generate tokens using python script
cd tokens
python generate_token.py
```

Verify if the tokens are correct

```bash
cd tokens
python verify_tokens.py
```

Note: This will generate tokens based on the JWT secret inside of the .env file

# Seed Data

- Run npm run dev: seed AFTER setting up DB and running the backend
- Replace DATA_DIR in seed-data.sh and create_auth_users.py to use sample-data-mini
