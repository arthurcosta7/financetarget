create table authentication_attempt_window (
    key_hash char(64) primary key,
    window_started_at timestamptz not null,
    expires_at timestamptz not null,
    attempts integer not null check (attempts > 0)
);

create index authentication_attempt_expiry_idx on authentication_attempt_window(expires_at);

update app_metadata set metadata_value = '6' where metadata_key = 'schema_version';
