-- Выполняется PostgreSQL автоматически при первом старте контейнера
-- (только если том pgdata пустой, т.е. при чистом запуске)

-- Триггер: первый зарегистрированный пользователь получает роль ADMIN
CREATE OR REPLACE FUNCTION fn_assign_first_user_admin()
RETURNS TRIGGER AS $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM users) THEN
        NEW.role_id := (SELECT id FROM user_role WHERE name = 'ADMIN');
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER trg_first_user_admin
    BEFORE INSERT ON users
    FOR EACH ROW
    EXECUTE FUNCTION fn_assign_first_user_admin();
