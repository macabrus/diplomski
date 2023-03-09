create table if not exists problem(
    id int primary key
);

create table if not exists population(
    id int primary key,
    label text,
    problem_id integer,
    FOREIGN KEY (problem_id) REFERENCES problem(id)
);

create table run(
    id int primary key,
    evolution_state JSON,
    is_finished BOOLEAN,
    data_points JSON
);