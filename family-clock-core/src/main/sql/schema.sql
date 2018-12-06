create table member
(
	id int auto_increment
		primary key,
	name char(2) not null,
	offset int not null,
	fg_color varchar(12) not null null,
	bg_color varchar(12) not null null
)
;

create table tracking
(
	member_id int not null,
	time timestamp not null on update CURRENT_TIMESTAMP,
	lat double not null,
	lon double not null,
	acc double null,
	alt double not null,
	vac double null
)
;

create index tracking_member_id_index
	on tracking (member_id)
;

create index tracking_time_index
	on tracking (time)
;

create table location
(
	id int auto_increment
		primary key,
	owner_id int null,
	name varchar(32) null,
	lon double null,
	lat double null,
	radius double null,
	priority int not null
)
;

create index location_owner_id_index
	on location (owner_id)
;

