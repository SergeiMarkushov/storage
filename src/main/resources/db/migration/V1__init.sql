create schema if not exists cloud_storage;
create table if not exists cloud_storage.my_users
(
    id        bigserial primary key,
    username  varchar(36) unique,
    full_name varchar(255),
    is_active boolean
);

create table if not exists cloud_storage.file_metadata
(
    id          bigserial primary key,
    user_id     bigint       not null,
    file_name   varchar(255) not null,
    file_size   bigint       not null,
    file_type   varchar(50),
    upload_date timestamp default current_timestamp,
    foreign key (user_id) references my_users (id)
);

create table if not exists cloud_storage.encryption_key
(
    id             varchar(36) primary key,
    file_id        bigint not null,
    encryption_key text   not null,
    foreign key (file_id) references file_metadata (id)
);

create table if not exists cloud_storage.file_actions
(
    id        varchar(36) primary key,
    file_id   bigint not null,
    user_id   bigint not null,
    action    bigint not null,
    timestamp timestamp default current_timestamp,
    details   text,
    foreign key (file_id) references file_metadata (id),
    foreign key (user_id) references my_users (id)
);