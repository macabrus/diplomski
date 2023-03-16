
import itertools
import re
from .models import Problem
import tsplib95

single_line_key_ex = re.compile(r"^(?P<key>[A-Z][A-Z0-9_]*):\s+(?P<val>.*)$")
multi_line_key_ex = re.compile(r"^(?P<key>[A-Z][A-Z0-9_]*)$")

def tsplib_parse(problem_str: str) -> Problem:
    tsp_problem = tsplib95.parse(problem_str)
    problem = Problem(
        label=tsp_problem.name,
        description=tsp_problem.comment
    )
    graph = tsp_problem.get_graph(normalize=True)
    problem.costs = [[None for j in range(len(graph.nodes))] for i in range(len(graph.nodes))]
    for i, j in itertools.product(list(graph.nodes), list(graph.nodes)):
        problem.costs[i][j] = graph.edges[i, j]['weight']
    for node in graph.nodes:
        problem.display[node] = graph.nodes[node]['display']
    print(problem)
    return problem


