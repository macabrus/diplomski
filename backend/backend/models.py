
import random
from datetime import datetime
from enum import Enum, unique
from typing import Any

from attr import define, field


def rand_color():
    def r():
        return random.randint(0, 255)
    return f'#{r():02x}{r():02x}{r():02x}'

# currently implemented only models for MTSP problems
# but any problem can be implemented in same manner


@define(kw_only=True)
class Problem:
    label: str
    description: str
    id: int = None
    color: str = field(factory=rand_color)
    depots: list[int]  # currently, we focus on single home depot problems
    costs: list[list[int]] = None
    # present: list[list[bool]] = field(factory=)
    # costs: dict[tuple[int, int], float] = field(factory=dict)
    # optional structure for visualizing the problem
    display: dict[int, Any] = field(factory=dict)


@define(kw_only=True)
class Fitness:
    max_tour_length: float
    total_length: float


@define(kw_only=True)
class Tour:
    depot: int
    tour: list[int] = field(factory=list)


@define(kw_only=True)
class Solution:
    phenotype: list[Tour]
    fitness: Fitness = None


@define(kw_only=True)
class Population:
    label: str
    individuals: list[Solution] = field(factory=list)
    id: int
    problem_id: int
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
    sharing_frequency: float
    ignore_rank_probability: float
    stop_after_generations: int | None
    stop_after_steady_generations: int
    mutation_operators: list[str]
    crossover_operators: list[str]


@define(kw_only=True)
class EvolutionState:
    generation: int
    iteration: int
    population: Population | None


@define(kw_only=True)
class Metrics:
    fitness: list[DataPoint] = field(factory=list)


@unique
class Status(Enum):
    PENDING = 'PENDING'
    RUNNING = 'RUNNING'
    FINISHED = 'FINISHED'


@define(kw_only=True)
class Worker:
    host: str
    slots: int
    used_slots: int = 0


@define(kw_only=True)
class Run:
    id: int = None
    problem_id: int = None
    population_id: int = None

    label: str
    problem: Problem
    state: EvolutionState
    config: EvolutionConfig

    status: Status = Status.PENDING
    metrics: Metrics = field(factory=Metrics)
