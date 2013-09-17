set client_min_messages to warning;

drop type if exists map_type cascade;

create type map_type as enum (
	'twisted_treeline',
	'summoners_rift',
	'howling_abyss'
);

drop type if exists match_type_type cascade;

create type match_type_type as enum (
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
	name text not null
);

-- Index for looking up summoners based summoner IDs in requests by users and also to resolve other players within a match
create index summoner_id_index on summoner (summoner_id);

drop table if exists match cascade;

-- Matches are stored in a redundant way, one time for each player that is being tracked
-- This is done because the systems available hardly enable one to tell if two match in two different match histories are actually the same
create table match(
	id serial primary key,
	-- The player whose perspective the match is being recorded from
	summoner_id integer references summoner(id) not null,
	-- The map the match was played on
	map map_type not null,
	-- Queue type or custom game mode indicator
	match_type match_type_type not null,
	-- The time the match finished, as UTC
	time timestamp not null,
	-- True if the player won the match
	victory boolean not null,
	-- Champion played by the player
	champion_id integer not null,
	-- Kills, deaths and assists of the player
	kills integer not null,
	deaths integer not null,
	assists integer not null,
	-- Array of IDs of items owned by the player
	-- Null entries indicate an empty slot
	-- Warning, size of array is ignored by the DBMS
	items integer[6] not null,
	-- Gold earned by the player
	gold integer not null,
	-- Number of minions killed by the player
	minions_killed integer not null,
	-- Duration of the match, in seconds
	duration integer not null,
	-- Summoner IDs of other players in the game
	-- The victorious flag could be calculated dynamically from this data but it would likely slow down some queries
	losing_team integer[] not null,
	winning_team integer[] not null
);

-- Index for looking up matches played by a player
create index match_summoner_id_index on match (summoner_id);

-- This big index is used to determine if a game was already recorded in the database
create index match_record_index on match (summoner_id, map, match_type, time, victory, champion_id, kills, deaths, assists, items, gold, minions_killed, duration, losing_team, winning_team);

drop table if exists aggregated_statistics cascade;

-- Aggregated statistics of players, by game mode and by champion, for all matches recorded
create table aggregated_statistics(
	id serial primary key,
	-- The player the stats are being recorded for
	summoner_id integer references summoner(id) not null,
	-- The map the matches were played on
	map map_type not null,
	-- Queue type or custom game mode indicator for the matches
	match_type match_type_type not null,
	-- Champion played by the player
	champion_id integer not null,
	-- Number of wins and losses with this configuration
	wins integer not null,
	losses integer not null,
	-- Total number of kills, deaths and assists of the player in these matches
	kills integer not null,
	deaths integer not null,
	assists integer not null,
	-- Total amount of gold earned by the player in these matches
	gold integer not null,
	-- Total number of minions killed by the player in these matches
	minions_killed integer not null,
	-- Total duration of all matches for this configuration, in seconds
	duration integer not null
);

-- Index for looking up records of a player for a certain mode
create index aggregated_statistics_lookup_index on aggregated_statistics (map, match_type, summoner_id);