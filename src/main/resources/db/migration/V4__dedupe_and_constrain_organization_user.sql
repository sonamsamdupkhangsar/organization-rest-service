with ranked_organization_users as (
    select id,
           row_number() over (
               partition by organization_id, user_id
               order by id
           ) as row_number
    from organization_user
    where organization_id is not null
      and user_id is not null
)
delete from organization_user organization_user
using ranked_organization_users
where organization_user.id = ranked_organization_users.id
  and ranked_organization_users.row_number > 1;

create unique index if not exists organization_user_organization_id_user_id_unique_idx
    on organization_user (organization_id, user_id)
    where organization_id is not null and user_id is not null;
