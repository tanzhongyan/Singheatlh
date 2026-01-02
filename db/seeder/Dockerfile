FROM python:3.11-slim

WORKDIR /app

# Install postgresql-client for psql commands
RUN apt-get update && \
    apt-get install -y postgresql-client && \
    rm -rf /var/lib/apt/lists/*

# Copy requirements
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy seeding scripts and data
COPY seeder.py .
COPY create_auth_users.py .
COPY sample-data/ ./sample-data/

CMD ["python", "seeder.py"]
