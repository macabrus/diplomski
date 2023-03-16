import base64 as b64
import json
import sqlite3
from contextlib import asynccontextmanager
from datetime import time

import aiofiles
import aiosqlite
from backend.dtos import ShortPopulation
from cattrs import register_structure_hook, unstructure
from starlette.applications import Starlette
from starlette.endpoints import WebSocketEndpoint
from starlette.requests import Request
from starlette.responses import JSONResponse
from starlette.routing import Route, WebSocketRoute
from starlette.types import Receive, Scope, Send
from starlette.websockets import WebSocket

from backend.models import Problem
from backend.parsers import tsplib_parse
from backend.populations import (generate_population, rotate_population,
                                 two_opt_population)
from backend.utils import prettify_sql

from . import repo


DB = 'app.db'

register_structure_hook(Problem, lambda o, c: c(**dict(o)))

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
    removed = await repo.remove_problem(req.app.state.db, req.path_params['id'])
    return JSONResponse(unstructure(removed))

async def list_problems(req: Request):
    return JSONResponse(unstructure(await repo.list_problems(req.app.state.db)))

async def create_population(req: Request):
    payload = await req.json()
    problem = await repo.get_problem(req.app.state.db, payload['problem_id'])
    pop = generate_population(problem, size=payload['size'], )
    if payload['two_opt']:
        pop = two_opt_population(problem, pop)
    if payload['align']:
        pop = rotate_population(problem, pop)
    pop = await repo.add_population(req.app.state.db, pop)
    return JSONResponse(unstructure(pop))

async def list_populations(req: Request):
    format = req.query_params.get('format', None)
    if format == 'short':
        populations = await repo.list_populations_short(req.app.state.db)
        return JSONResponse(unstructure(populations))
    return JSONResponse([])

async def remove_population():
    ...
@asynccontextmanager
async def lifespan(app: Starlette):
    print(f"Connecting to local db: {DB}")
    async with aiosqlite.connect(DB, detect_types=sqlite3.PARSE_DECLTYPES) as db:
        await db.set_trace_callback(lambda sql: print(prettify_sql(sql)))
        await db.execute('pragma journal_mode = wal')
        aiosqlite.register_adapter(dict, json.dumps)
        aiosqlite.register_adapter(list, json.dumps)
        aiosqlite.register_converter('json', json.loads)
        db.row_factory = aiosqlite.Row
        async with aiofiles.open('sql/schema.sql') as schema:
            await db.executescript(await schema.read())
        app.state.db = db
        yield
        await db.commit()
        # async with aiofiles.open('sql/drop.sql') as schema:
        #    await db.executescript(await schema.read())
    print(f"Disconnecting from local db: {DB}")

empty_list = []
# endpoint for handling streaming of data points when algo is running
class StreamEndpoint(WebSocketEndpoint):
    encoding = 'json'
    def __init__(self, scope, receive, send):
        super().__init__(scope, receive, send)
        self.subs = {}
    
    async def on_connect(self, ws: WebSocket) -> None:
        await ws.accept() # accept connection
        print(f"[{time()}] connected: {ws.client}")

    async def on_disconnect(self, ws: WebSocket, close_code: int) -> None:
        for run_id, clients in self.subs.items():
            self.subs[run_id] = [c for c in clients if c['ws'] is not ws]
            if not self.subs[run_id]:
                self.subs.pop(run_id)
        print(f"[{time()}] disconnected: {ws.client} with code: {close_code}")
    
    async def on_receive(self, ws: WebSocket, message: dict) -> None:
        match message['type']:
            case 'pub':
                pass  # just trust it, todo: add some kind of sec check
            case 'sub':
                descriptor = {'ws': ws, 'offset': message['offset']}
                if message['run_id'] not in self.subs:
                    self.subs[message['run_id']] = []
                self.subs[message['run_id']].append(descriptor)
            case 'update':
                for sub in self.subs.get(message['run_id'], empty_list):
                    if sub['offset'] < message['index']:
                        await sub['ws'].send_json(message)
            case 'checkpoint':
                pass  # persist state from message back into db

# propagates incomming messages from producer processes to interested
# clients on the frontend
async def handle_ws(scope: Scope, receive: Receive, send: Send):
    subs = {}
    if not hasattr(scope.state, 'subs'):
        scope.state.subs = subs
    ws = WebSocket(scope=scope, receive=receive, send=send)
    await ws.accept()
    # identifier info from client
    client_info = await ws.receive_json()
    await ws.send_text('Hello, world!')
    await ws.close()

async def add_population(req: Request):
    payload = await req.json()
    size = payload.get('size', 50)
    problem_id = payload.get('problem_id', None)
    two_opt = payload.get('two_opt', False)
    rotate = payload.get('rotate', False)
    if problem_id is None:
        raise
    problem = await repo.get_problem(req.app.state.db, problem_id)
    population = generate_population(problem, size=size, two_opt=two_opt)
    if two_opt:
        population = two_opt_population(population)
    if rotate:
        population = rotate_population(population)
    print(population)
    return JSONResponse(None)

app = Starlette(
    debug=True,
    routes=[
        Route('/problem', add_problem, methods=['POST']),
        Route('/problem', list_problems, methods=['GET']),
        Route('/problem/{id}', remove_problem, methods=['DELETE']),
        Route('/population', list_populations, methods=['GET']),
        Route('/population', add_population, methods=['POST']),
        Route('/population/{id}', remove_population, methods=['DELETE']),
        WebSocketRoute('/stream', StreamEndpoint)
    ],
    lifespan=lifespan
)
