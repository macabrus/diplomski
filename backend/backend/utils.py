from typing import Any

import prettyprinter
import sqlparse
from pygments import highlight
from pygments.formatters.terminal256 import Terminal256Formatter
from pygments.lexers.sql import SqlLexer
from pygments.styles import get_style_by_name

prettyprinter.install_extras(["attrs"])
prettyprinter.set_default_style("dark")

lexer = SqlLexer()
formatter = Terminal256Formatter(style=get_style_by_name("monokai"))


def prettify_sql(sql: str) -> str:
    sql = sqlparse.format(sql, reindent=True, keyword_case="upper")
    sql = highlight(sql, lexer, formatter)
    return sql

def prettify_attrs(obj: Any) -> str:
    return prettyprinter.cpprint(obj, end=" ")
