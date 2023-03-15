
from backend.models import Population, Problem


def generate_population(problem: Problem, size=10, two_opt=True, rotate=True) -> Population:
    ...

def two_opt_population(population): 
    ...

def rotate_population(problem, population):
    ...