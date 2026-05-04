alter table concert_entity
    add notified_whatsapp boolean default true not null;
alter table concert_entity
    add notified_telegram boolean default true not null;

