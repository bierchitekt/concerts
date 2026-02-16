alter table concert_entity
    add constraint concert_link_date_uk
        unique (link, date);
