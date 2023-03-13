from contextlib import asynccontextmanager
from distutils.log import debug
from backend.parsers import tsplib_parse
from starlette.applications import Starlette
from starlette.routing import Route, Mount
from starlette.requests import Request
from starlette.responses import JSONResponse
from cattrs import structure
import aiosqlite
import aiofiles
import uvicorn
from .models import Problem
from . import repo

DB = 'app.db'

async def add_problem(req: Request):
    payload = await req.json()
    problem = tsplib_parse(payload['file'][0]['content'])
    problem.label = payload['label'] or problem.label
    problem.descirption = payload['description'] or problem.descirption
    return JSONResponse(await repo.add_problem(req.app.state.db, problem))


async def remove_problem(req: Request):
    ...

async def list_problems(req: Request):
    db = req.app.state.db
    ...

@asynccontextmanager
async def lifespan(app: Starlette):
    print(f"Connecting to local db: {DB}")
    async with aiosqlite.connect(DB) as db:
        async with aiofiles.open('sql/schema.sql') as schema:
            await db.executescript(await schema.read())
        app.state.db = db
        yield
        async with aiofiles.open('sql/drop.sql') as schema:
            await db.executescript(await schema.read())
    print(f"Disconnecting from local db: {DB}")


app = Starlette(
    debug=True,
    routes=[Route('/problem', add_problem, methods=['POST'])],
    lifespan=lifespan
)
