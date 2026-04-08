-- Ensure wwssdb and n8n databases exist. Uses psql's \gexec to run the
-- CREATE DATABASE only when the SELECT finds the database is missing.
SELECT 'CREATE DATABASE wwssdb' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'wwssdb')\gexec
SELECT 'CREATE DATABASE n8n' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'n8n')\gexec

