create table if not exists user_default_organization(
    user_id uuid primary key,
    organization_id uuid not null,
    created timestamp,
    updated timestamp
);

create index if not exists user_default_organization_organization_id_idx
    on user_default_organization (organization_id);
