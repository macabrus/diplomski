
import itertools
import re
from .models import Problem
import tsplib95

single_line_key_ex = re.compile(r"^(?P<key>[A-Z][A-Z0-9_]*):\s+(?P<val>.*)$")
multi_line_key_ex = re.compile(r"^(?P<key>[A-Z][A-Z0-9_]*)$")


def tsplib_parse(problem_str: str) -> Problem:
    tsp_problem = tsplib95.parse(problem_str)

    graph = tsp_problem.get_graph(normalize=True)
    display = {}
    depots = []
    costs = [[None for j in range(len(graph.nodes))]
             for i in range(len(graph.nodes))]
    for i, j in itertools.product(list(graph.nodes), list(graph.nodes)):
        costs[i][j] = graph.edges[i, j]['weight']
    for node in graph.nodes:
        display[node] = graph.nodes[node]['display']
        # in current implementation only single home depot is supported
        # but it is easily tweaked to support multiple depots
        if graph.nodes[node]['is_depot']:
            depots.append(node)
    # if home depot is not provided, use 0 as default
    if not depots:
        depots.append(0)
    problem = Problem(
        label=tsp_problem.name,
        description=tsp_problem.comment,
        costs=costs,
        depots=depots,
        display=display
    )
    print(problem)
    return problem
