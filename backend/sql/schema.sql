create table if not exists problem(
    id integer primary key autoincrement,
    label text,
    description text,
    color text,
    costs json
);

create table if not exists population(
    id integer primary key autoincrement,
    label text,
    problem_id integer
);

create table if not exists run(
    id integer primary key AUTOINCREMENT,
    problem_id integer,
    evolution_state json,
    status boolean,
    data_points json
);