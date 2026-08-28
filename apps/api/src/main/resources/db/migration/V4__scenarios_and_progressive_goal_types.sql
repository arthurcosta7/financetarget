alter table goal drop constraint goal_goal_type_check;
alter table goal add constraint goal_goal_type_check check (
    goal_type in ('HOME_DOWN_PAYMENT','EMERGENCY_RESERVE','VEHICLE','TRAVEL','CUSTOM')
);

create table scenario (
    id uuid primary key,
    space_id uuid not null references planning_space(id),
    goal_id uuid not null references goal(id),
    created_by uuid not null references app_user(id),
    title varchar(80) not null,
    target_date date not null,
    annual_inflation_rate numeric(12, 8) not null check (annual_inflation_rate > -1),
    annual_return_rate numeric(12, 8) not null check (annual_return_rate > -1),
    contribution_timing varchar(24) not null check (contribution_timing in ('END_OF_MONTH','BEGINNING_OF_MONTH')),
    created_at timestamptz not null
);
create index scenario_goal_created_idx on scenario(goal_id, created_at, id);

alter table calculation_snapshot add column scenario_id uuid references scenario(id);
alter table calculation_snapshot drop constraint calculation_snapshot_origin_check;
alter table calculation_snapshot add constraint calculation_snapshot_origin_check
    check (origin in ('GOAL_CREATED','SCENARIO_CREATED'));
create unique index calculation_snapshot_scenario_idx on calculation_snapshot(scenario_id)
    where scenario_id is not null;

update app_metadata set metadata_value = '4' where metadata_key = 'schema_version';
