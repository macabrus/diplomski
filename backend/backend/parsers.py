
import re
from .models import Problem

def tsplib_parse(problem_str: str) -> Problem:

    # parse into key-value dict:
    data = {}
    lines = problem_str.split('\n')
    for i, line in enumerate(lines):
        single_line_key_ex = re.compile(r"^(?<key>[A-Z][A-Z0-9_]*):\s+(?<val>.*)$")
        multi_line_key_ex = re.compile(r"^(?<key>[A-Z][A-Z0-9_]*)$")
        if m := single_line_key_ex.match(line):
            data[m.group('key')] = m.group('val')
        elif m := multi_line_key_ex.match(line):
            key = m.group('key')
            for l in enumerate(lines[i+1:], start=i+1):
                if single_line_key_ex.match(l) or multi_line_key_ex(l):
                    break
                if key not in data:
                    data[key] = ''
                data[key] += l
    problem = Problem(label=data['NAME'], descirption=data['COMMENT'])


