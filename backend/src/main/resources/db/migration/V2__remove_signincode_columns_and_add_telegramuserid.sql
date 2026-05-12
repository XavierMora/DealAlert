ALTER TABLE account DROP COLUMN sign_in_code;
ALTER TABLE account DROP COLUMN sign_in_code_expected_expiration;
ALTER TABLE account DROP COLUMN last_sign_in_code_sent_at;

ALTER TABLE account ADD COLUMN telegram_user_id bigint;