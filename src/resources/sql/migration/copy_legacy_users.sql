INSERT INTO users_new (login_id, password, name, job)
SELECT id, password, name, job
FROM users;
