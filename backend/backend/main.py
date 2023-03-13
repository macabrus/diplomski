import base64 as b64
from contextlib import asynccontextmanager
from backend.parsers import tsplib_parse
from backend.populations import generate_population
from starlette.applications import Starlette
from starlette.routing import Route
from starlette.requests import Request
from starlette.responses import JSONResponse
import aiosqlite
import aiofiles
from . import repo
from cattrs import unstructure, register_structure_hook
import json

DB = 'app.db'

register_structure_hook(aiosqlite.Row, lambda o, c: c(**o))

async def add_problem(req: Request):
    payload = await req.json()
    file_content = (
        b64.decodebytes(str.encode(payload['file'][0]['content']))
        .decode('utf8')
    )
    problem = tsplib_parse(file_content)
    problem.label = payload.get('label', problem.label)
    problem.color = payload.get('color', problem.color)
    problem.description = payload.get('description', problem.description)
    return JSONResponse(unstructure(await repo.add_problem(req.app.state.db, problem)))

async def remove_problem(req: Request):
    return JSONResponse(
        await repo.remove_problem(req.app.state.db, req.path_params['id'])
    )

async def list_problems(req: Request):
    return JSONResponse(unstructure(await repo.list_problems(req.app.state.db)))

@asynccontextmanager
async def lifespan(app: Starlette):
    print(f"Connecting to local db: {DB}")
    async with aiosqlite.connect(DB) as db:
        await db.set_trace_callback(print)
        aiosqlite.register_adapter(dict, lambda d: json.dumps(d))
        aiosqlite.register_converter('json', lambda d: json.loads(d))
        db.row_factory = aiosqlite.Row
        async with aiofiles.open('sql/schema.sql') as schema:
            await db.executescript(await schema.read())
        app.state.db = db
        yield
        #async with aiofiles.open('sql/drop.sql') as schema:
        #    await db.executescript(await schema.read())
    print(f"Disconnecting from local db: {DB}")

async def add_population(req: Request):
    payload = await req.json()
    size = payload.get('size', 50)
    problem_id = payload.get('problem_id', None)
    if problem_id is None:
        raise
    problem = repo.get_problem(req.app.state.db, problem_id)
    strategy = payload.get('strategy', ['random'])
    generate_population()

app = Starlette(
    debug=True,
    routes=[
        Route('/problem', add_problem, methods=['POST']),
        Route('/problem', list_problems, methods=['GET']),
        Route('/problem/{id}', list_problems, methods=['DELETE']),
        Route('/population', add_population)
    ],
    lifespan=lifespan
)
