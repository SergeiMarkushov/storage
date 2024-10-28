create schema if not exists cloud_storage;
create table if not exists cloud_storage.my_users
(
    id        bigserial primary key,
    username  varchar(36) unique,
    full_name varchar(255),
    is_active boolean
);

comment on table cloud_storage.my_users is 'Таблица учета пользователей сервиса';
comment on column cloud_storage.my_users.id is 'Идентификатор записи';
comment on column cloud_storage.my_users.username is 'Псевдоним пользователя';
comment on column cloud_storage.my_users.full_name is 'Имя и фамилия пользователя';
comment on column cloud_storage.my_users.is_active is 'Флаг активности пользователя';

create table if not exists cloud_storage.file_metadata
(
    id          bigserial primary key,
    user_id     bigint       not null,
    file_name   varchar(255) not null,
    file_size   bigint       not null,
    file_type   varchar(50),
    upload_date timestamp with time zone default now(),
    foreign key (user_id) references my_users (id)
);

comment on table cloud_storage.file_metadata is 'Таблица учета мета данных файлов';
comment on column cloud_storage.file_metadata.id is 'Идентификатор записи';
comment on column cloud_storage.file_metadata.user_id is 'Идентификатор владельца файла';
comment on column cloud_storage.file_metadata.file_name is 'Имя файла';
comment on column cloud_storage.file_metadata.file_size is 'Размер файла';
comment on column cloud_storage.file_metadata.file_type is 'Тип файла';
comment on column cloud_storage.file_metadata.upload_date is 'Дата и время сохранения файла';

create table if not exists cloud_storage.allowed_files
(
    user_id bigint not null references my_users (id),
    file_id bigint not null references file_metadata (id),
    primary key (user_id, file_id)
);

comment on table cloud_storage.allowed_files is 'Таблица учета мета данных файлов';
comment on column cloud_storage.allowed_files.user_id is 'Идентификатор пользователя, которому разрешен доступ к файлу';
comment on column cloud_storage.allowed_files.file_id is 'Идентификатор файла, к которому разрешен доступ пользователю';

create table if not exists cloud_storage.encryption_keys
(
    id             bigserial primary key,
    file_id        bigint not null,
    encryption_key text   not null,
    foreign key (file_id) references file_metadata (id)
);

comment on table cloud_storage.encryption_keys is 'Таблица для хранения ключей шифрования файлов';
comment on column cloud_storage.encryption_keys.id is 'Идентификатор записи';
comment on column cloud_storage.encryption_keys.file_id is 'Идентификатор файла';
comment on column cloud_storage.encryption_keys.encryption_key is 'Ключ шифрования файла';

create table if not exists cloud_storage.file_actions
(
    id        varchar(36) primary key,
    file_id   bigint not null,
    user_id   bigint not null,
    action    bigint not null,
    timestamp timestamp with time zone default now(),
    details   text,
    foreign key (file_id) references file_metadata (id),
    foreign key (user_id) references my_users (id)
);

comment on table cloud_storage.file_actions is 'Таблица учета действий с файлами';
comment on column cloud_storage.file_actions.id is 'Идентификатор записи';
comment on column cloud_storage.file_actions.file_id is 'Идентификатор файла';
comment on column cloud_storage.file_actions.user_id is 'Идентификатор пользователя';
comment on column cloud_storage.file_actions.action is 'Действие с файлом';
comment on column cloud_storage.file_actions.timestamp is 'Дата и время действия с файлом';
comment on column cloud_storage.file_actions.details is 'Комментарий к действиям с файлом';