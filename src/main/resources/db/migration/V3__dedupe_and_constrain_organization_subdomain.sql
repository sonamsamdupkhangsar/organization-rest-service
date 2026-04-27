with ranked_organizations as (
    select id,
           subdomain,
           first_value(id) over (
               partition by subdomain
               order by created nulls last, id
           ) as keep_id,
           row_number() over (
               partition by subdomain
               order by created nulls last, id
           ) as row_number
    from organization
    where subdomain is not null
      and trim(subdomain) <> ''
),
duplicate_organizations as (
    select id as duplicate_id, keep_id
    from ranked_organizations
    where row_number > 1
)
update organization_user organization_user
set organization_id = duplicate_organizations.keep_id
from duplicate_organizations
where organization_user.organization_id = duplicate_organizations.duplicate_id;

with ranked_organizations as (
    select id,
           subdomain,
           first_value(id) over (
               partition by subdomain
               order by created nulls last, id
           ) as keep_id,
           row_number() over (
               partition by subdomain
               order by created nulls last, id
           ) as row_number
    from organization
    where subdomain is not null
      and trim(subdomain) <> ''
),
duplicate_organizations as (
    select id as duplicate_id, keep_id
    from ranked_organizations
    where row_number > 1
)
update organization_position organization_position
set organization_id = duplicate_organizations.keep_id
from duplicate_organizations
where organization_position.organization_id = duplicate_organizations.duplicate_id;

with ranked_organizations as (
    select id,
           subdomain,
           row_number() over (
               partition by subdomain
               order by created nulls last, id
           ) as row_number
    from organization
    where subdomain is not null
      and trim(subdomain) <> ''
)
delete from organization organization
using ranked_organizations
where organization.id = ranked_organizations.id
  and ranked_organizations.row_number > 1;

create unique index if not exists organization_subdomain_unique_idx
    on organization (subdomain)
    where subdomain is not null and trim(subdomain) <> '';
