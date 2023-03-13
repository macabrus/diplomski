from unittest import skip
from attr import astuple
from .models import Problem
from aiosqlite import Connection
from attrs import fields


def csv_slots(model):
    return ", ".join("?" for f in fields(model) if f.name not in skip)

def csv_keys(model, skip=set()):
    return ", ".join(f.name for f in fields(model) if f.name not in skip)


async def add_problem(db: Connection,  problem: Problem) -> Problem:
    rows = await db.execute_fetchall(
        f'''
        insert into problem({csv_keys(Problem, skip=('id',))})
        values ({csv_slots(Problem, skip=('id',))})
        ''', astuple(problem))
    problem.id = rows[0]['id']
    return problem

async def remove_problem(db: Connection, id: int) -> Problem:
    ...