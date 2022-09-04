create table if not exists Organization(id uuid primary key, name varchar, created timestamp)

create table if not exists Organization_user(id uuid primary key, organization_id uuid, user_id uuid)