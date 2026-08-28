create table subscription_plan (
    code varchar(64) primary key,
    display_name varchar(120) not null,
    status varchar(16) not null check (status in ('ACTIVE','INACTIVE')),
    created_at timestamptz not null
);

create table plan_entitlement (
    plan_code varchar(64) not null references subscription_plan(code),
    entitlement_key varchar(80) not null,
    entitlement_value varchar(120) not null,
    primary key (plan_code, entitlement_key)
);

create table account_subscription (
    id uuid primary key,
    user_id uuid not null unique references app_user(id),
    plan_code varchar(64) not null references subscription_plan(code),
    status varchar(24) not null check (status in ('ACTIVE','PAST_DUE','CANCELED')),
    provider varchar(32) not null,
    provider_subscription_ref varchar(160),
    version bigint not null default 0,
    updated_at timestamptz not null,
    unique (provider, provider_subscription_ref)
);

create table checkout_session (
    id uuid primary key,
    user_id uuid not null references app_user(id),
    plan_code varchar(64) not null references subscription_plan(code),
    provider varchar(32) not null,
    provider_reference varchar(160) not null,
    idempotency_key varchar(128) not null,
    request_hash char(64) not null,
    status varchar(24) not null check (status in ('SIMULATED','EXPIRED')),
    created_at timestamptz not null,
    unique (user_id, idempotency_key),
    unique (provider, provider_reference)
);

create table payment_webhook_event (
    id uuid primary key,
    provider varchar(32) not null,
    event_id varchar(160) not null,
    event_type varchar(80) not null,
    payload_hash char(64) not null,
    status varchar(24) not null check (status in ('RECEIVED','PROCESSED','REJECTED')),
    received_at timestamptz not null,
    processed_at timestamptz,
    unique (provider, event_id)
);

create table notification_preference (
    user_id uuid not null references app_user(id),
    category varchar(32) not null check (category in ('ESSENTIAL','PLANNING_REMINDERS','PRODUCT_UPDATES','MARKETING')),
    email_enabled boolean not null check (category <> 'ESSENTIAL' or email_enabled),
    updated_at timestamptz not null,
    primary key (user_id, category)
);

create table notification_intent (
    id uuid primary key,
    user_id uuid not null references app_user(id),
    category varchar(32) not null,
    template_key varchar(80) not null,
    channel varchar(24) not null check (channel in ('EMAIL')),
    status varchar(24) not null check (status in ('SIMULATED','SUPPRESSED','DISABLED')),
    created_at timestamptz not null
);
create index notification_intent_user_idx on notification_intent(user_id, created_at);

update app_metadata set metadata_value = '5' where metadata_key = 'schema_version';
