-- 开发期固定身份使用的本地用户。仅用于开发环境；生产禁用开发身份。
-- 详见 docs/decisions/0001-identity-and-data-ownership.md。
INSERT IGNORE INTO app_user (id, username, display_name)
VALUES (1, 'local', '本地开发用户');
