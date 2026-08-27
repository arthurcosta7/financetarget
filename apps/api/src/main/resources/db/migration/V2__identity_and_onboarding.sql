create table app_user (
    id uuid primary key,
    email_normalized varchar(320) not null unique,
    display_name varchar(120) not null,
    status varchar(24) not null check (status in ('PENDING_VERIFICATION', 'ACTIVE', 'DELETION_REQUESTED')),
    email_verified_at timestamptz,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create table credential (
    user_id uuid primary key references app_user(id),
    password_hash varchar(255) not null,
    password_version bigint not null default 1,
    changed_at timestamptz not null
);

create table identity_token (
    id uuid primary key,
    user_id uuid not null references app_user(id),
    purpose varchar(24) not null check (purpose in ('VERIFY_EMAIL', 'RESET_PASSWORD')),
    token_hash char(64) not null unique,
    expires_at timestamptz not null,
    consumed_at timestamptz,
    created_at timestamptz not null
);
create index identity_token_user_purpose_idx on identity_token(user_id, purpose);

create table session_family (
    id uuid primary key,
    user_id uuid not null references app_user(id),
    created_at timestamptz not null,
    last_seen_at timestamptz not null,
    revoked_at timestamptz,
    revocation_reason varchar(64)
);
create index session_family_user_idx on session_family(user_id);

create table access_token (
    id uuid primary key,
    family_id uuid not null references session_family(id),
    token_hash char(64) not null unique,
    expires_at timestamptz not null,
    created_at timestamptz not null
);
create index access_token_family_idx on access_token(family_id);

create table refresh_token (
    id uuid primary key,
    family_id uuid not null references session_family(id),
    token_hash char(64) not null unique,
    expires_at timestamptz not null,
    consumed_at timestamptz,
    created_at timestamptz not null
);
create index refresh_token_family_idx on refresh_token(family_id);

create table planning_space (
    id uuid primary key,
    type varchar(16) not null check (type in ('PERSONAL', 'SHARED')),
    name varchar(120) not null,
    base_currency char(3) not null,
    status varchar(16) not null check (status in ('ACTIVE', 'ARCHIVED')),
    version bigint not null default 0,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create table space_member (
    space_id uuid not null references planning_space(id),
    user_id uuid not null references app_user(id),
    role varchar(16) not null check (role in ('OWNER', 'EDITOR', 'VIEWER')),
    status varchar(16) not null check (status in ('ACTIVE', 'INVITED', 'REMOVED')),
    joined_at timestamptz not null,
    primary key (space_id, user_id)
);
create index space_member_user_idx on space_member(user_id, status);

create table financial_profile (
    space_id uuid primary key references planning_space(id),
    recurring_income numeric(19, 2) not null check (recurring_income >= 0),
    essential_expenses numeric(19, 2) not null check (essential_expenses >= 0),
    initial_goal_balance numeric(19, 2) not null check (initial_goal_balance >= 0),
    suggested_monthly_capacity numeric(19, 2) not null check (suggested_monthly_capacity >= 0),
    confirmed_monthly_capacity numeric(19, 2) not null check (confirmed_monthly_capacity >= 0),
    currency char(3) not null,
    reference_date date not null,
    version bigint not null default 0,
    updated_at timestamptz not null
);

create table consent_record (
    id uuid primary key,
    user_id uuid not null references app_user(id),
    purpose varchar(64) not null,
    document_version varchar(32) not null,
    decision varchar(16) not null check (decision in ('GRANTED', 'REVOKED')),
    source varchar(32) not null,
    recorded_at timestamptz not null
);
create index consent_record_user_idx on consent_record(user_id, recorded_at);

create table data_subject_request (
    id uuid primary key,
    user_id uuid not null references app_user(id),
    request_type varchar(24) not null check (request_type in ('EXPORT', 'DELETION')),
    status varchar(24) not null check (status in ('COMPLETED', 'REQUESTED', 'BLOCKED')),
    idempotency_key varchar(128) not null,
    created_at timestamptz not null,
    completed_at timestamptz,
    unique(user_id, request_type, idempotency_key)
);

create table audit_event (
    id uuid primary key,
    actor_user_id uuid references app_user(id),
    action varchar(80) not null,
    resource_type varchar(48) not null,
    resource_id uuid,
    outcome varchar(16) not null check (outcome in ('SUCCESS', 'DENIED', 'FAILED')),
    trace_id varchar(80),
    occurred_at timestamptz not null
);
create index audit_event_actor_idx on audit_event(actor_user_id, occurred_at);

update app_metadata set metadata_value = '2' where metadata_key = 'schema_version';
