-- Grant all privileges to test user
ALTER USER test WITH SUPERUSER;
GRANT ALL PRIVILEGES ON DATABASE testdb TO test;
GRANT ALL PRIVILEGES ON SCHEMA public TO test;

