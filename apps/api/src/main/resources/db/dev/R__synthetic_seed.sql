insert into app_metadata (metadata_key, metadata_value)
values ('seed_profile', 'synthetic-development-data')
on conflict (metadata_key)
do update set
    metadata_value = excluded.metadata_value,
    updated_at = current_timestamp;

insert into subscription_plan(code, display_name, status, created_at)
values ('LOCAL_ESSENTIAL', 'Essencial local', 'ACTIVE', current_timestamp),
       ('LOCAL_COMPLETE', 'Completo local', 'ACTIVE', current_timestamp)
on conflict (code) do update set display_name=excluded.display_name, status=excluded.status;

insert into plan_entitlement(plan_code, entitlement_key, entitlement_value)
values ('LOCAL_ESSENTIAL', 'GOAL_MANAGEMENT', 'enabled'),
       ('LOCAL_ESSENTIAL', 'SCENARIO_LIMIT', '1'),
       ('LOCAL_COMPLETE', 'GOAL_MANAGEMENT', 'enabled'),
       ('LOCAL_COMPLETE', 'SCENARIO_LIMIT', '3'),
       ('LOCAL_COMPLETE', 'SHARED_PLANNING', 'enabled')
on conflict (plan_code, entitlement_key)
do update set entitlement_value=excluded.entitlement_value;
