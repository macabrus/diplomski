
import random
from backend.models import Population, Problem


def generate_population(problem: Problem, size=10, two_opt=True, rotate=True) -> Population:
    for i in range(size):
        # random.shuffle()
        ...
    ...

def two_opt_population(population: Population): 
    ...

def rotate_population(population: Population):
    ...