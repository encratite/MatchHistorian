set client_min_messages to warning;

drop type if exists map_type cascade;

create type map_type as enum (
	'twisted_treeline',
	'summoners_rift',
	'howling_abyss'
);

drop type if exists game_mode_type cascade;

create type game_mode_type as enum (
	'normal',
	'ranked_solo',
	'ranked_team',
	'custom'
);

drop table if exists summoner cascade;

create table summoner(
	id serial primary key,
	-- A string that specifies the region such as 'na', 'euw' or 'eune'
	region text not null,
	-- Numeric ID of the summoner in that region
	summoner_id integer not null,
	-- Last known name of that summoner
	name text not null,
	-- If true, the match history of this summoner will be checked regularly for updates
	update_automatically boolean not null
);

-- Index for looking up summoners based summoner IDs in requests by users and also to resolve other players within a game
create index summoner_id_index on summoner (summoner_id);

drop table if exists game cascade;

create table game(
	id serial primary key,
	-- Region the game was played on
	region text not null,
	-- ID of the game on the server
	game_id integer not null,
	-- The map the game was played on
	map map_type not null,
	-- Queue type or custom game mode indicator
	game_mode game_mode_type not null,
	-- The time the game finished, as UTC
	time timestamp not null,
	-- Duration of the game, in seconds
	duration integer not null,
	-- Summoner IDs of other players in the game
	losing_team integer[] not null,
	winning_team integer[] not null
);

-- Index for checking if a match is already in the database
create index game_region_game_id_index on game (region, game_id);

-- Information about a  particular player that participated in a game
create table game_player(
	id serial primary key,
	-- The game this record is associated with
	game_id integer references game(id),
	-- The summoner the stats are associated with
	summoner_id integer references summoner(id),
	-- Champion played by the player
	champion_id integer not null,
	-- The following fields may all be null because they are not available for any of the other players in a match
	-- Summoner spells
	spells integer[2],
	-- Kills, deaths and assists of the player
	kills integer,
	deaths integer,
	assists integer,
	-- Array of IDs of items owned by the player
	-- Null entries indicate an empty slot
	-- Warning, size of array is ignored by the DBMS
	items integer[6],
	-- Gold earned by the player
	gold integer,
	-- Number of minions killed by the player
	minions_killed integer,
);

drop table if exists aggregated_statistics cascade;

-- Aggregated statistics of players, by game mode and by champion, for all games recorded
create table aggregated_statistics(
	id serial primary key,
	-- The player the stats are being recorded for
	summoner_id integer references summoner(id) not null,
	-- The map the games were played on
	map map_type not null,
	-- Queue type or custom game mode indicator for the games
	game_mode game_mode_type not null,
	-- Champion played by the player
	champion_id integer not null,
	-- Number of wins and losses with this configuration
	wins integer not null,
	losses integer not null,
	-- Total number of kills, deaths and assists of the player in these games
	kills integer not null,
	deaths integer not null,
	assists integer not null,
	-- Total amount of gold earned by the player in these games
	gold integer not null,
	-- Total number of minions killed by the player in these games
	minions_killed integer not null,
	-- Total duration of all games for this configuration, in seconds
	duration integer not null
);

-- Index for looking up records of a player for a certain mode
create index aggregated_statistics_lookup_index on aggregated_statistics (map, game_type, summoner_id);