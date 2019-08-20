# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table account (
  id                            bigint auto_increment not null,
  customer_id                   bigint not null,
  type                          varchar(255),
  routing_number                bigint not null,
  account_number                bigint not null,
  balance                       double not null,
  interest                      double not null,
  constraint pk_account primary key (id)
);

create table address (
  id                            bigint auto_increment not null,
  address1                      varchar(255),
  address2                      varchar(255),
  city                          varchar(255),
  state                         varchar(255),
  zip_code                      varchar(255),
  customer_id                   bigint,
  constraint uq_address_customer_id unique (customer_id),
  constraint pk_address primary key (id)
);

create table customer (
  id                            bigint auto_increment not null,
  customer_id                   varchar(255),
  client_id                     integer not null,
  first_name                    varchar(255),
  last_name                     varchar(255),
  date_of_birth                 timestamp,
  ssn                           varchar(255),
  social_insurancenum           varchar(255),
  tin                           varchar(255),
  phone_number                  varchar(255),
  address_id                    bigint,
  constraint uq_customer_address_id unique (address_id),
  constraint pk_customer primary key (id)
);

create table patient (
  id                            bigint auto_increment not null,
  patient_id                    integer not null,
  patient_first_name            varchar(255),
  patient_last_name             varchar(255),
  date_of_birth                 timestamp,
  patient_weight                integer not null,
  patient_height                integer not null,
  medications                   varchar(255),
  body_temp_deg_c               integer not null,
  heart_rate                    integer not null,
  pulse_rate                    integer not null,
  bp_diastolic                  integer not null,
  constraint pk_patient primary key (id)
);

alter table account add constraint fk_account_customer_id foreign key (customer_id) references customer (id) on delete restrict on update restrict;
create index ix_account_customer_id on account (customer_id);

alter table address add constraint fk_address_customer_id foreign key (customer_id) references customer (id) on delete restrict on update restrict;

alter table customer add constraint fk_customer_address_id foreign key (address_id) references address (id) on delete restrict on update restrict;


# --- !Downs

alter table account drop constraint if exists fk_account_customer_id;
drop index if exists ix_account_customer_id;

alter table address drop constraint if exists fk_address_customer_id;

alter table customer drop constraint if exists fk_customer_address_id;

drop table if exists account;

drop table if exists address;

drop table if exists customer;

drop table if exists patient;

