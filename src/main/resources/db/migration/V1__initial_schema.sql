create table if not exists organization_position(
    id uuid primary key,
    organization_id uuid,
    name varchar
);

create table if not exists organization(
    id uuid primary key,
    name varchar,
    creator_user_id uuid,
    created timestamp
);

create table if not exists organization_user(
    id uuid primary key,
    organization_id uuid,
    user_id uuid,
    position_id uuid
);

create unique index if not exists organization_user_organization_id_user_id_unique_idx
    on organization_user (organization_id, user_id)
    where organization_id is not null and user_id is not null;

create table if not exists subdomain(
    id uuid primary key,
    host varchar not null,
    created timestamp
);

create unique index if not exists subdomain_host_unique_idx
    on subdomain (host);

create table if not exists subdomain_organization(
    id uuid primary key,
    subdomain_id uuid not null,
    organization_id uuid not null,
    created timestamp
);

create unique index if not exists subdomain_organization_unique_idx
    on subdomain_organization (subdomain_id, organization_id);

create table if not exists user_default_organization(
    user_id uuid primary key,
    organization_id uuid not null,
    created timestamp,
    updated timestamp
);

create index if not exists user_default_organization_organization_id_idx
    on user_default_organization (organization_id);
