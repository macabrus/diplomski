
import json
from aiosqlite import Connection, Cursor
from attrs import fields
from cattrs import structure, unstructure
from rich.pretty import pprint
from backend.dtos import ShortPopulation

from .models import EvolutionConfig, EvolutionState, Population, Problem, Run


def csv_slots(model, skip=set()):
    return ", ".join(f":{f.name}" for f in fields(model) if f.name not in skip)


def csv_keys(model, skip=set()):
    return ", ".join(f.name for f in fields(model) if f.name not in skip)

# bag some props into subdict


def bag(obj, bag_key, keys=set()):
    bag = obj[bag_key] = {}
    for key in keys:
        bag[key] = obj.pop(key)
    return obj


def unbag(obj, bag_key):
    ...


def filter_keys(obj, keys=set()):
    for key in keys:
        obj.pop(key)
    return obj


async def add_problem(db: Connection,  problem: Problem) -> Problem:
    sql = f'''
    insert into problem({
        csv_keys(Problem, skip=('id',))
    })
    values ({csv_slots(Problem, skip=('id',))})
    returning id
    '''
    async with db.cursor() as cur:
        if await has_problem_label(cur, problem.label):
            raise
        await cur.execute(sql, unstructure(problem))
        problem.id = (await cur.fetchall())[0]['id']
        return problem


async def has_problem_label(db: Connection, name: str) -> bool:
    sql = 'select 1 from problem where label = ?'
    await db.execute(sql, (name,))
    return bool(await db.fetchall())


async def remove_problem(db: Connection, id: int) -> Problem | None:
    sql = 'delete from problem where id = ? returning *'
    async with db.cursor() as cur:
        if await has_active_depenendent_runs(cur, id):
            raise
        await cur.execute(sql, (id,))
        rows = await cur.fetchall()
        if not rows:
            return None
        return structure(rows[0], Problem)


async def get_problem(db: Cursor, id: int) -> Problem:
    await db.execute('select * from problem where id = ?', (id,))
    rows = await db.fetchall()
    return structure(rows[0], Problem)


async def list_problems(db: Cursor) -> list[Problem]:
    await db.execute('select * from problem')
    rows = list(await db.fetchall())
    return list(map(lambda row: structure(row, Problem), rows))


async def has_problem(db: Connection, id: int):
    sql = 'select 1 from problem where id = ?'
    return await db.execute(sql, (id,))

# checks if problem can be removed. It can only be removed if no active
# run depends on it


async def has_active_depenendent_runs(db: Connection, problem_id: int):
    sql = 'select * from run where problem_id = ? and status != "FINISHED"'
    await db.execute(sql, (problem_id,))
    return bool(await db.fetchall())


async def add_population(db: Cursor, population: Population) -> Population:
    sql = f'''
    insert into population(
    {csv_keys(Population, skip=('id', 'problem', 'num_salesmen'))}, "_data"
    ) values(
    {csv_slots(Population, skip=('id', 'problem', 'num_salesmen'))}, :_data
    )
    returning id
    '''
    if await has_population_label(db, population.label):
        raise
    row = unstructure(population)
    row['_data'] = {'num_salesmen': row.pop('num_salesmen')}
    pprint(row)
    await db.execute(sql, row)
    population.id = (await db.fetchall())[0]['id']
    return population


async def has_population_label(db: Cursor, label: str):
    sql = 'select 1 from population where id = ?'
    await db.execute(sql, (label, ))
    return bool(await db.fetchall())


async def remove_population(db: Connection, pop_id: int) -> Population | None:
    sql = 'delete from population where id = ?'
    return await db.execute_fetchall(sql, (pop_id,))


async def get_population(db: Connection, pop_id: int) -> Population | None:
    sql = 'select * from population where id = ?'
    await db.execute(sql, (pop_id,))
    rows = await db.fetchall()
    row = dict(rows[0])
    row |= row.pop('_data')
    if not rows:
        return None
    return structure(row, Population)


async def list_populations(db: Cursor, short=False) -> list[ShortPopulation]:
    if short:
        await db.execute('select id, label from population')
        rows = await db.fetchall()
        return [structure(row, ShortPopulation) for row in rows]
    else:
        sql = 'select id, label, individuals from population'
        rows = await db.execute_fetchall(sql)
        rows = [dict(row) for row in rows]
        return [structure(row, Population) for row in rows]


async def add_run(db: Cursor,  run: Run):
    await db.execute(f'''
        insert into run(
        {csv_keys(Run, skip=('id', 'problem', 'population'))})
        values (
        {csv_slots(Run, skip=('id', 'problem', 'population'))})
        returning id
    ''', unstructure(run))
    row = dict((await db.fetchall())[0])
    run.id = row['id']
    return run
    # row |= {'problem': await get_problem(db, row['problem_id'])}
    # row |= {'population': await get_population(db, row['population_id'])}
    # pprint(row)
    # return structure(row, Run)


async def remove_run(db: Connection, id: int):
    sql = f'delete from run where id = ?'
    ...

# gets run DETAILS


async def get_run(db: Cursor):
    sql = '''
    select * from run
    left join population as pop
    left join problem as prob
    left join
    '''
    print('here')

async def list_runs(db: Cursor):
    await db.execute('select * from run')
    rows = await db.fetchall()
    print(json.dumps([dict(row) for row in rows]))

# updates evolution state of a run with data
# received from runner subprocess


async def update_run(db: Connection, state: EvolutionState):
    sql = f'''
    insert into run({csv_keys(EvolutionState)}) values ({csv_slots(EvolutionState)})
    '''
