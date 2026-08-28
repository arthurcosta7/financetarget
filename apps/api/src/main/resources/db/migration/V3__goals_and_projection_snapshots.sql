create table goal (
    id uuid primary key,
    space_id uuid not null references planning_space(id),
    created_by uuid not null references app_user(id),
    goal_type varchar(32) not null check (goal_type in ('HOME_DOWN_PAYMENT')),
    title varchar(120) not null,
    target_amount numeric(19, 2) not null check (target_amount > 0),
    target_value_basis varchar(24) not null check (target_value_basis in ('CURRENT_VALUE', 'FIXED_NOMINAL')),
    target_date date not null,
    initial_balance numeric(19, 2) not null check (initial_balance >= 0),
    annual_inflation_rate numeric(12, 8) not null check (annual_inflation_rate > -1),
    annual_return_rate numeric(12, 8) not null check (annual_return_rate > -1),
    contribution_timing varchar(24) not null check (contribution_timing in ('END_OF_MONTH', 'BEGINNING_OF_MONTH')),
    planned_monthly_contribution numeric(19, 2) not null check (planned_monthly_contribution >= 0),
    currency char(3) not null,
    status varchar(16) not null check (status in ('ACTIVE', 'ARCHIVED')),
    version bigint not null default 0,
    created_at timestamptz not null,
    updated_at timestamptz not null
);
create index goal_space_status_idx on goal(space_id, status, created_at);

create table calculation_snapshot (
    id uuid primary key,
    space_id uuid not null references planning_space(id),
    goal_id uuid not null references goal(id),
    created_by uuid not null references app_user(id),
    input_hash char(64) not null,
    engine_version varchar(40) not null,
    formula_version varchar(40) not null,
    normalized_inputs jsonb not null,
    projection_result jsonb not null,
    warnings jsonb not null,
    origin varchar(32) not null check (origin in ('GOAL_CREATED')),
    created_at timestamptz not null
);
create index calculation_snapshot_goal_idx on calculation_snapshot(goal_id, created_at desc);

create table contribution (
    id uuid primary key,
    space_id uuid not null references planning_space(id),
    goal_id uuid not null references goal(id),
    created_by uuid not null references app_user(id),
    amount numeric(19, 2) not null check (amount > 0),
    currency char(3) not null,
    contribution_date date not null,
    note varchar(240),
    idempotency_key varchar(128) not null,
    created_at timestamptz not null,
    unique(goal_id, created_by, idempotency_key)
);
create index contribution_goal_date_idx on contribution(goal_id, contribution_date, created_at);

update app_metadata set metadata_value = '3' where metadata_key = 'schema_version';
