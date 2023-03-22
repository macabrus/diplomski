import asyncio
import base64 as b64
import json
import sqlite3
from contextlib import asynccontextmanager
from datetime import time
from uuid import uuid4

import aiofiles
import aiosqlite
from cattrs import register_structure_hook, structure, unstructure
from starlette.applications import Starlette
from starlette.endpoints import WebSocketEndpoint
from starlette.requests import Request
from starlette.responses import JSONResponse
from starlette.routing import Route, WebSocketRoute
from starlette.types import Receive, Scope, Send
from starlette.websockets import WebSocket

from backend.dtos import ShortPopulation
from backend.models import Problem, Run, Worker
from backend.parsers import tsplib_parse
from backend.populations import generate_population
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
    return JSONResponse(
        unstructure(await repo.add_problem(req.app.state.db, problem))
    )

async def remove_problem(req: Request):
    removed = await repo.remove_problem(req.app.state.db, req.path_params['id'])
    return JSONResponse(unstructure(removed))

async def list_problems(req: Request):
    return JSONResponse(unstructure(await repo.list_problems(req.app.state.db)))

async def list_populations(req: Request):
    format = req.query_params.get('format', None)
    if format == 'short':
        populations = await repo.list_populations_short(req.app.state.db)
        print(populations)
        return JSONResponse(unstructure(populations))
    return JSONResponse([])

async def add_population(req: Request):
    payload = await req.json()
    label = payload['label']
    problem_id = payload.get('problem_id', None)
    size = int(payload['size'])
    num_salesmen = int(payload['num_salesmen'])
    two_opt = payload.get('two_opt', False)
    rotate = payload.get('rotate', False)
    if problem_id is None:
        raise
    problem = await repo.get_problem(req.app.state.db, problem_id)
    population = generate_population(problem, num_salesmen, size)
    population.label = label
    population.problem_id = problem_id
    population.problem = problem
    print(population)
    await repo.add_population(req.app.state.db, population)
    return JSONResponse(unstructure(population))

async def remove_population(req: Request):
    id = int(req.path_params['id'])
    return JSONResponse(await repo.remove_population(req.app.state.db, id))

async def list_runs(req: Request):
    return JSONResponse([])

async def add_run(req: Request):
    payload = await req.json()
    print(payload)
    await repo.add_run(req.app.state.db, Run())
    return JSONResponse({})

async def remove_run():
    raise NotImplemented

async def start_run():
    raise NotImplemented

async def pause_run():
    raise NotImplemented

async def cancel_run():
    raise NotImplemented

async def list_workers(req: Request):
    return JSONResponse(unstructure(req.app.state.workers))

# invoked by runners only with evolution state as body
async def save_evolution(req: Request):
    ...

@asynccontextmanager
async def lifespan(app: Starlette):
    print('Initialize runner slots')
    app.state.subs = {}
    app.state.workers: list[Worker] = []
    aiosqlite.register_adapter(dict, json.dumps)
    aiosqlite.register_adapter(list, json.dumps)
    aiosqlite.register_converter('json', json.loads)
    print(f"Connecting to local db: {DB}")
    async with aiosqlite.connect(DB, detect_types=sqlite3.PARSE_DECLTYPES) as db:
        await db.set_trace_callback(lambda sql: print(prettify_sql(sql)))
        await db.execute('pragma journal_mode = wal')
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
        self.subs = scope['app'].state.subs
    
    async def on_connect(self, ws: WebSocket) -> None:
        await ws.accept() # accept connection
        print(f"[{time()}] connected: {ws.client}")

    async def on_disconnect(self, ws: WebSocket, close_code: int) -> None:
        for run_id, clients in self.subs.items():
            self.subs[run_id] = [c for c in clients if c['ws'] is not ws]
            if not self.subs[run_id]:
                self.subs.pop(run_id)
        for i, runner in enumerate(ws.app.state.workers):
            if runner.host == ws.client.host:
                runner = ws.app.state.workers.pop(i)
                print(f'removed runner: {runner}')
                break
        print(f"[{time()}] disconnected: {ws.client} with code: {close_code}")
    
    async def on_receive(self, ws: WebSocket, message: dict) -> None:
        print(f'received: {message}')
        match message['type']:
            case 'pub':
                pass  # just trust it, todo: add some kind of sec check
            case 'sub':
                if message['run_id'] not in self.subs:
                    self.subs[message['run_id']] = []
                self.subs[message['run_id']].append({'ws': ws})
            case 'update':
                for sub in self.subs.get(message['run_id'], empty_list):
                    await sub['ws'].send_json(message)
            case 'runner':
                workers = ws.app.state.workers
                if message['action'] == 'register':
                    worker = {**message, 'host': ws.client.host}
                    workers.append(structure(worker, Worker))
            case _:
                raise ValueError(f'Unknown message: {message}')

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


app = Starlette(
    debug=True,
    routes=[
        # problem api
        Route('/problem', add_problem, methods=['POST']),
        Route('/problem', list_problems, methods=['GET']),
        Route('/problem/{id}', remove_problem, methods=['DELETE']),

        # population api
        Route('/population', list_populations, methods=['GET']),
        Route('/population', add_population, methods=['POST']),
        Route('/population/{id}', remove_population, methods=['DELETE']),

        # run api
        Route('/run', list_runs, methods=['GET']),
        Route('/run', add_run, methods=['POST']),
        Route('/run/{id}', remove_run, methods=['DELETE']),

        # run management
        Route('/run/{id}/start', start_run, methods=['PUT']),
        Route('/run/{id}/pause', pause_run, methods=['PUT']),
        Route('/run/{id}/cancel', cancel_run, methods=['PUT']),

        # checkpoint run
        Route('/run/{id}', save_evolution, methods=['PUT']),

        # api for listing available workers
        Route('/worker', list_workers, methods=['GET']),

        # for tracking runners and clients
        WebSocketRoute('/stream', StreamEndpoint)
    ],
    lifespan=lifespan
)
