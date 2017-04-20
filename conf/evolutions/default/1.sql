# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table channel (
  channelid                     integer auto_increment not null,
  currentviewers                integer,
  totalviews                    integer,
  channeltype                   varchar(3),
  streamkey                     varchar(16),
  roomid                        integer,
  userid                        integer,
  constraint uq_channel_roomid unique (roomid),
  constraint uq_channel_userid unique (userid),
  constraint pk_channel primary key (channelid)
);

create table chat (
  chatid                        integer auto_increment not null,
  senderid                      integer,
  roomid                        integer,
  sentdate                      date,
  senttime                      time,
  message                       varchar(255),
  constraint pk_chat primary key (chatid)
);

create table chatroom (
  roomid                        integer auto_increment not null,
  currentchatters               integer,
  chatcount                     integer,
  publicroom                    tinyint(1) default 0,
  constraint pk_chatroom primary key (roomid)
);

create table user (
  userid                        integer auto_increment not null,
  username                      varchar(50),
  password                      varchar(64),
  email                         varchar(64),
  constraint pk_user primary key (userid)
);

alter table channel add constraint fk_channel_roomid foreign key (roomid) references chatroom (roomid) on delete restrict on update restrict;

alter table channel add constraint fk_channel_userid foreign key (userid) references user (userid) on delete restrict on update restrict;

alter table chat add constraint fk_chat_senderid foreign key (senderid) references user (userid) on delete restrict on update restrict;
create index ix_chat_senderid on chat (senderid);

alter table chat add constraint fk_chat_roomid foreign key (roomid) references chatroom (roomid) on delete restrict on update restrict;
create index ix_chat_roomid on chat (roomid);


# --- !Downs

alter table channel drop foreign key fk_channel_roomid;

alter table channel drop foreign key fk_channel_userid;

alter table chat drop foreign key fk_chat_senderid;
drop index ix_chat_senderid on chat;

alter table chat drop foreign key fk_chat_roomid;
drop index ix_chat_roomid on chat;

drop table if exists channel;

drop table if exists chat;

drop table if exists chatroom;

drop table if exists user;

