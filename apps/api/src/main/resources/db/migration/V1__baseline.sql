create table app_metadata (
    metadata_key varchar(100) primary key,
    metadata_value varchar(500) not null,
    updated_at timestamptz not null default current_timestamp
);

insert into app_metadata (metadata_key, metadata_value)
values ('schema_version', '1');
