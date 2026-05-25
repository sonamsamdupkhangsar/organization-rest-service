drop index if exists organization_subdomain_unique_idx;

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

insert into subdomain(id, host, created)
select (
           substr(md5(lower(trim(subdomain))), 1, 8) || '-' ||
           substr(md5(lower(trim(subdomain))), 9, 4) || '-' ||
           substr(md5(lower(trim(subdomain))), 13, 4) || '-' ||
           substr(md5(lower(trim(subdomain))), 17, 4) || '-' ||
           substr(md5(lower(trim(subdomain))), 21, 12)
       )::uuid,
       lower(trim(subdomain)),
       min(created)
from organization
where subdomain is not null
  and trim(subdomain) <> ''
group by lower(trim(subdomain))
on conflict (host) do nothing;

insert into subdomain_organization(id, subdomain_id, organization_id, created)
select (
           substr(md5(subdomain.id::text || ':' || organization.id::text), 1, 8) || '-' ||
           substr(md5(subdomain.id::text || ':' || organization.id::text), 9, 4) || '-' ||
           substr(md5(subdomain.id::text || ':' || organization.id::text), 13, 4) || '-' ||
           substr(md5(subdomain.id::text || ':' || organization.id::text), 17, 4) || '-' ||
           substr(md5(subdomain.id::text || ':' || organization.id::text), 21, 12)
       )::uuid,
       subdomain.id,
       organization.id,
       organization.created
from organization
join subdomain on subdomain.host = lower(trim(organization.subdomain))
where organization.subdomain is not null
  and trim(organization.subdomain) <> ''
on conflict (subdomain_id, organization_id) do nothing;

alter table organization drop column if exists subdomain;
