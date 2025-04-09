-- auto-generated definition
create table concert_entity
(
    date          date,
    notified      boolean                         not null,
    id            varchar(255)                    not null
        primary key,
    link          varchar(255),
    location      varchar(255),
    support_bands varchar(255),
    title         varchar(255),
    genre         jsonb,
    added_at      date default '2025-03-15'::date not null,
    price         varchar                         not null default ''
);

create unique index unique_title_date
    on concert_entity (title, date);

CREATE EXTENSION pg_trgm;
