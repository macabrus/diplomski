from unittest import skip
from attr import astuple
from .models import Problem
from aiosqlite import Connection, Cursor
from attrs import fields
from cattrs import structure


def csv_slots(model, skip=set()):
    return ", ".join("?" for f in fields(model) if f.name not in skip)

def csv_keys(model, skip=set()):
    return ", ".join(f.name for f in fields(model) if f.name not in skip)


async def add_problem(db: Connection,  problem: Problem) -> Problem:
    rows = await db.execute_fetchall(f'''
        insert into problem({csv_keys(Problem, skip=('id',))})
        values ({csv_slots(Problem, skip=('id',))})
        returning id
        ''', astuple(problem, filter=lambda f, v: f.name not in ('id',)))
    print(dict(**rows[0]))
    problem.id = rows[0]['id']
    return problem

async def remove_problem(db: Connection, id: int) -> Problem:
    async with db.cursor() as cur:
        if has_active_depenendent_runs(db, id):
            raise
        rows = await db.execute_fetchall('delete from problem where id = ? returning *', (id,))
        return structure(rows[0], Problem)

async def get_problem(db: Connection, id: int) -> Problem:
    rows = await db.execute_fetchall('select * from problem where id = ?', (id,))
    return structure(rows[0], Problem)

async def list_problems(db: Connection) -> list[Problem]:
    rows = await db.execute_fetchall('select * from problem')
    return list(map(lambda row: structure(row, Problem), rows))


async def has_active_depenendent_runs(db: Connection, id: int):
    return bool(await db.execute('select * from runs where problem_id = ?', (id,)))

