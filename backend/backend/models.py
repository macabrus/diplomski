
from datetime import datetime
from enum import Enum, unique
from attr import define, field
import random

@define(kw_only=True)
class Problem:
    label: str
    description: str
    id: int = None
    color: str = field(factory=lambda: '#{:02x}{:02x}{:02x}'.format(*map(lambda x: random.randint(0, 255), range(3))))
    costs: dict[tuple[int, int], float] = field(factory=dict)

@define(kw_only=True)
class Solution:
    fitness: tuple[float, float]
    phenotype: list[int]

@define(kw_only=True)
class Population:
    individuals: list[Solution]
    problem: Problem
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

@define(kw_only=True)
class EvolutionState:
    generation: int
    iteration: int
    population: list[Solution]
    problem: Problem
    config: EvolutionConfig

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
    status: Status
    last_state: EvolutionState
    metrics: Metrics
