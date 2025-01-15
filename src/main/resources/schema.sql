CREATE TABLE public.concert_entity (
	"date" date NULL,
	notified bool NOT NULL,
	id varchar(255) NOT NULL,
	link varchar(255) NULL,
	"location" varchar(255) NULL,
	support_bands varchar(255) NULL,
	title varchar(255) NULL,
	genre jsonb NULL,
	CONSTRAINT concert_entity_pkey PRIMARY KEY (id)
);

CREATE UNIQUE INDEX unique_title_date ON concert_entity(title, date);
