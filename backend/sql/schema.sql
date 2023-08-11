create table if not exists problem(
    id integer primary key autoincrement,
    label text,
    description text,
    color text,
    depots json,
    costs json,
    display json
);

create table if not exists population(
    id integer primary key autoincrement,
    label text,
    problem_id integer,
    individuals json,
    "_data" json
);

create table if not exists run(
    id integer primary key AUTOINCREMENT,
    label text,
    problem_id integer,
    population_id integer,
    "state" json,
    "config" json,
    "status" boolean,
    metrics json
);