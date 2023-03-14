import base64 as b64
import json
import typing
from contextlib import asynccontextmanager
from datetime import time
from http import client

import aiofiles
import aiosqlite
from cattrs import register_structure_hook, unstructure
from starlette.applications import Starlette
from starlette.endpoints import WebSocketEndpoint
from starlette.requests import Request
from starlette.responses import JSONResponse
from starlette.routing import Route, WebSocketRoute
from starlette.types import Receive, Scope, Send
from starlette.websockets import WebSocket

from backend.parsers import tsplib_parse
from backend.populations import generate_population

from . import repo

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

# endpoint for handling streaming of 
class StreamEndpoint(WebSocketEndpoint):
    encoding = 'json'
    def __init__(self, scope, receive, send):
        super().__init__(scope, receive, send)
        # tracking subscriptions

        # 1. client loads already stored data from run
        # 2. subscribes as 'sub' with offet of last datapoint + 1
        # 3. receives all newer data points
        # {'type': 'pub', 'run_id': 1, 'offset': 1000}
        # {'type': 'sub', 'run_id': 1, 'offset': 1000}
        # {'type': 'update', 'run_id': 1, 'index': 1001}
        self.subs = {}
        ...
    
    async def on_connect(self, ws: WebSocket) -> None:
        await ws.accept() # accept connection
        print(f"[{time()}] connected: {ws.client}")

    async def on_disconnect(self, ws: WebSocket, close_code: int) -> None:
        for topic, clients in self.subs:
            self.subs[topic] = [c for c in clients if c['ws'] is not ws]
            if not self.subs[topic]:
                self.subs.pop(topic)
        print(f"[{time()}] disconnected: {ws.client} with code: {close_code}")
    
    async def on_receive(self, ws: WebSocket, message: dict) -> None:
        match message['type']:
            case 'pub':
                ...
            case 'sub':
                descriptor = {'ws': ws, 'offset': message['offset']}
                if message['run_id'] not in self.subs:
                    self.subs[message['run_id']] = []
                self.subs[message['run_id']].append(descriptor)
            case 'update':
                for sub in self.subs.get(message['run_id'], empty_list):
                    if sub['offset'] < message['index']:
                        sub['ws'].send_json(message)

empty_list = []
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
        Route('/population', add_population),
        WebSocketRoute('/stream', StreamEndpoint)
    ],
    lifespan=lifespan
)
