
import re
from .models import Problem


single_line_key_ex = re.compile(r"^(?P<key>[A-Z][A-Z0-9_]*):\s+(?P<val>.*)$")
multi_line_key_ex = re.compile(r"^(?P<key>[A-Z][A-Z0-9_]*)$")

def tsplib_parse(problem_str: str) -> Problem:
    # parse into key-value dict:
    data = {}
    lines = problem_str.split('\n')
    for i, line in enumerate(lines):
        if m := single_line_key_ex.match(line):
            data[m.group('key')] = m.group('val')
        elif m := multi_line_key_ex.match(line):
            key = m.group('key')
            for l in lines[i+1:]:
                if single_line_key_ex.match(l) or multi_line_key_ex.match(l):
                    break
                if key not in data:
                    data[key] = ''
                data[key] += l
    problem = Problem(
        label=data.get('NAME', None),
        description=data.get('COMMENT', None)
    )
    print(problem)
    return problem


