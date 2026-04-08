#!/bin/bash
set -e

# This script will be executed by the official Postgres image during initialization
# It creates the wwssdb and n8n databases if they do not already exist. The
# psql \gexec trick evaluates the returned CREATE DATABASE statement only when
# the SELECT finds the database is absent, making this idempotent.

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname=postgres <<-'EOSQL'
SELECT 'CREATE DATABASE wwssdb' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'wwssdb')\gexec
SELECT 'CREATE DATABASE n8n' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'n8n')\gexec
EOSQL

echo "Databases ensured: wwssdb, n8n"

