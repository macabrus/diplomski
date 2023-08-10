import json, os
import sqlite3
import time
from typing import AsyncIterator
import uuid
from contextlib import asynccontextmanager

import aiosqlite
import darkdetect
import sqlglot
# from app.contexts import Context
from pygments import highlight
from pygments.formatters.terminal256 import Terminal256Formatter
from pygments.formatters.html import HtmlFormatter
from pygments.lexers.sql import SqlLexer
from pygments.styles import get_style_by_name

lexer = SqlLexer()
formatter = Terminal256Formatter(style=get_style_by_name("tango" if darkdetect.isLight() else "monokai"))
# html_formatter = HtmlFormatter()


def fmt_sql(sql: str) -> str:
    sql = sqlglot.transpile(sql, read='sqlite', identify=True, pretty=True)[0].strip()
    return sql

def pretty_print_sql(sql: str) -> str:
    sql = sqlglot.transpile(sql, read='sqlite', identify=True, pretty=True)[0].strip()
    sql = highlight(sql, lexer, formatter)
    # sql = '--- QUERY ---\n' + sql
    print(sql)


@asynccontextmanager
async def connection_context(name = 'app.db') -> AsyncIterator[aiosqlite.Cursor]:
    from cattrs import global_converter as gconv
    
    gconv.register_structure_hook(aiosqlite.Row, lambda r: gconv.structure(dict(r)))

    aiosqlite.register_adapter(dict, json.dumps)
    aiosqlite.register_adapter(list, json.dumps)
    aiosqlite.register_converter('json', json.loads)
    db = await aiosqlite.connect(
        # 'app.db',
        name,
        uri=True,
        check_same_thread=False,
        detect_types=sqlite3.PARSE_DECLTYPES
    )
    await db.execute('pragma journal_mode = wal')
    await db.execute('pragma foreign_keys = on')
    db.isolation_level = None
    db.row_factory = aiosqlite.Row
    #if bool(os.environ.get('DEBUG', None)):
    await db.set_trace_callback(pretty_print_sql)
    cursor = await db.cursor()
    # if context is not None:
    #     context.connection = db
    #     context.db = await db.cursor()
    try:
        yield cursor
    except Exception as e:
        await db.rollback()
        await db.close()
        # if context is not None: # unset
        #     context.connection = None
        #     context.db = None
        raise e
    await db.commit()
    await db.close()
    # if context is not None: # unset
    #     context.connection = None
    #     context.db = None


class TransactionError(Exception):
    def __init__(self, message):
        super().__init__(message)


@asynccontextmanager
async def transaction_context(connection, savepoint_name='') -> AsyncIterator[aiosqlite.Cursor]:
    try:
        sid = savepoint_name + '_' + str(uuid.uuid4())[:8]
        # params = {'sid': sid}
        await connection.execute(f'SAVEPOINT {sid}')
        yield connection
        await connection.execute(f'RELEASE {sid}')
        # context.savepoints.append(sid)
        # await context.db.execute('SAVEPOINT :sid', params)
        # yield context.db
        # await context.db.execute('RELEASE :sid', params)
    except Exception as e:
        await connection.execute(f'ROLLBACK TO {sid}')
        raise e
        # if len(context.savepoints) == 1:
        #     context.db.execute('ROLLBACK')
        # elif len(context.savepoints) >= 2:
        #     context.savepoints.pop(-1) # current savepoint
        #     await context.db.execute('ROLLBACK TO :sid', params)
        #     await context.db.execute('RELEASE :sid', params)
        # else:
        #     raise TransactionError('Expected at least one savepoint in stack. Found none!')