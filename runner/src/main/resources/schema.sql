create table if not exists problem(
    id int primary key,
    label text,
    description text,
    color text,
    costs json
);

create table if not exists population(
    id int primary key,
    label text,
    problem_id integer
);

create table if not exists run(
    id int primary key,
    evolution_state json,
    status boolean,
    data_points json
);