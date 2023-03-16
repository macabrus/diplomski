
import random
from backend.models import Population, Problem


def generate_population(problem: Problem, salesmen=None, size=10, two_opt=True, rotate=True) -> Population:
    if salesmen is None:
        raise
    for i in range(size):
        for range(problem.costs)
        ...
    ...

def two_opt_population(population: Population): 
    ...

def rotate_population(population: Population):
    ...