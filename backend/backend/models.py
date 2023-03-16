
from datetime import datetime
from enum import Enum, unique
from typing import Any
from attr import define, field
import random

def rand_color():
    return '#{:02x}{:02x}{:02x}'.format(*map(lambda x: random.randint(0, 255), range(3)))

# currently implemented only models for MTSP problems
# but any problem can be implemented in same manner
@define(kw_only=True)
class Problem:
    label: str
    description: str
    id: int = None
    color: str = field(factory=rand_color)
    costs: list[list[int]] = None
    # present: list[list[bool]] = field(factory=)
    # costs: dict[tuple[int, int], float] = field(factory=dict)
    display: dict[int, Any] = field(factory=dict) # optional structure for visualizing the problem

@define(kw_only=True)
class Fitness:
    max_tour_length: float
    total_length: float

@define(kw_only=True)
class Solution:
    fitness: Fitness
    phenotype: list[list[int]]

@define(kw_only=True)
class Population:
    label: str
    individuals: list[Solution]
    problem_id: int = None
    problem: Problem = None
    ...

@define(kw_only=True)
class DataPoint:
    index: int
    value: float
    time_sampled: datetime.time

@define(kw_only=True)
class EvolutionConfig:
    mutation_probability: float
    sharing_distance: float
    stop_after_generations: int | None
    stop_after_steady_generations: int
    mutation_operators: list[str]
    crossover_operators: list[str]

@define(kw_only=True)
class EvolutionState:
    generation: int
    iteration: int
    population: list[Solution]

@define(kw_only=True)
class Metrics:
    fitness: list[DataPoint]

@unique
class Status(Enum):
    PENDING = 'PENDING'
    RUNNING = 'RUNNING'
    FINISHED = 'FINISHED'

@define(kw_only=True)
class Run:
    id: int
    status: Status
    problem: Problem
    state: EvolutionState
    config: EvolutionConfig
    metrics: Metrics
