docker exec -ti postgres-concerts /usr/bin/pg_dump  -U bierchitekt concerts > postgres-backup.sql

# restore
psql -U bierchitekt -d concerts < postgres-backup.sql