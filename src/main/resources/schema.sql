create table if not exists Organization_Position(id uuid primary key, organization_id uuid, name varchar);

create table if not exists Organization(id uuid primary key, name varchar, creator_user_id UUID, created timestamp);

create table if not exists "subdomain"(id uuid primary key, host varchar not null, created timestamp);

create table if not exists "subdomain_organization"(id uuid primary key, subdomain_id uuid not null, organization_id uuid not null, created timestamp);

create table if not exists Organization_user(id uuid primary key, organization_id uuid, user_id uuid, position_id uuid);

create table if not exists "user_default_organization"(user_id uuid primary key, organization_id uuid not null, created timestamp, updated timestamp)
