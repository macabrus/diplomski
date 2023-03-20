
from aiosqlite import Connection, Cursor
from attrs import fields
from cattrs import structure, unstructure

from backend.dtos import ShortPopulation

from .models import EvolutionState, Population, Problem, Run


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

async def get_problem(db: Connection, id: int) -> Problem:
    sql = 'select * from problem where id = ?'
    rows = await db.execute_fetchall(sql, (id,))
    return structure(rows[0], Problem)

async def list_problems(db: Connection) -> list[Problem]:
    sql = 'select * from problem'
    rows = list(await db.execute_fetchall(sql))
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


async def add_population(db: Connection, population: Population) -> Population:
    sql = f'''
    insert into population(
    {csv_keys(Population, skip=('id', 'problem'))}
    ) values(
    {csv_slots(Population, skip=('id', 'problem'))})
    returning id
    '''
    async with db.cursor() as cur:
        if await has_population_label(cur, population.label):
            raise
        await cur.execute(sql, unstructure(population))
        population.id = (await cur.fetchall())[0]['id']
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
    await db.execute(sql,(pop_id,))
    rows = await db.fetchall()
    if not rows:
        return None
    return structure(rows[0], Population)

# lists only label and id of population
async def list_populations_short(db: Connection) -> list[ShortPopulation]:
    sql = 'select id, label from population'
    rows = await db.execute_fetchall(sql)
    return list(map(lambda row: structure(row, ShortPopulation), rows))

async def add_run(db: Connection,  run: Run):
    sql = f'''
    insert into run(
    {csv_keys(Run, skip=('problem',))})
    values (
    {csv_slots(Run, skip=('problem',))})
    returning id
    '''
    async with db.cursor() as cur:
        population = await get_population()
        problem = population.problem
        await cur.execute(sql, unstructure(run))
        rows = await cur.fetchall()
        assert len(rows) == 1
        return structure(rows[0] | {'problem': problem, 'population': population}, Run)

# removes run
async def remove_run(db: Connection, id: int):
    sql = f'delete from run where id = ?'
    ...

# gets run DETAILS
async def get_run(db: Connection):
    sql = '''
    select * from run
    left join population as pop
    left join problem as prob
    left join
    '''

# updates evolution state of a run with data
# received from runner subprocess
async def update_run(db: Connection, state: EvolutionState):
    sql = f'''
    insert into run({csv_keys(EvolutionState)}) values ({csv_slots(EvolutionState)})
    '''

