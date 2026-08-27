insert into app_metadata (metadata_key, metadata_value)
values ('seed_profile', 'synthetic-development-data')
on conflict (metadata_key)
do update set
    metadata_value = excluded.metadata_value,
    updated_at = current_timestamp;
