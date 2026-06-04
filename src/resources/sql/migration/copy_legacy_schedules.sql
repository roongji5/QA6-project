INSERT INTO schedules_new (id, user_id, title, date, start_time, end_time, description, completed)
SELECT schedules.id,
       users_new.id,
       schedules.title,
       schedules.date,
       schedules.start_time,
       schedules.end_time,
       schedules.description,
       CASE WHEN schedules.completed IN (0, 1) THEN schedules.completed ELSE 0 END
FROM schedules
JOIN users_new ON users_new.login_id = schedules.user_id;
