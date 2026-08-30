create table space_invitation (
    id uuid primary key,
    space_id uuid not null references planning_space(id),
    invited_by uuid not null references app_user(id),
    email_normalized varchar(320) not null,
    role varchar(16) not null check (role in ('OWNER', 'EDITOR', 'VIEWER')),
    status varchar(16) not null check (status in ('PENDING', 'ACCEPTED', 'REJECTED', 'EXPIRED')),
    expires_at timestamptz not null,
    created_at timestamptz not null,
    responded_at timestamptz
);
create index space_invitation_recipient_idx on space_invitation(email_normalized, status, expires_at);
create unique index space_invitation_pending_unique on space_invitation(space_id, email_normalized)
    where status = 'PENDING';

create table beta_product_event (
    id uuid primary key,
    user_id uuid not null references app_user(id),
    event_name varchar(48) not null,
    journey_stage varchar(32) not null,
    outcome varchar(24) not null,
    device_class varchar(16) not null,
    occurred_at timestamptz not null
);
create index beta_product_event_time_idx on beta_product_event(occurred_at);

create table beta_feedback (
    id uuid primary key,
    user_id uuid not null references app_user(id),
    category varchar(32) not null,
    rating smallint check (rating between 1 and 5),
    comment varchar(500),
    status varchar(16) not null check (status in ('OPEN', 'REVIEWED')),
    created_at timestamptz not null
);
create index beta_feedback_created_idx on beta_feedback(created_at);

update app_metadata set metadata_value = '7' where metadata_key = 'schema_version';
