
import random
import sys

from backend.constrained_random import ConstrainedRandom
from backend.models import Fitness, Population, Problem, Solution, Tour


def generate_population(problem: Problem, salesmen: int, size: int) -> Population:
    pop = Population(label=None)
    cr = ConstrainedRandom(
        ranges=[(1, len(problem.costs)) for _ in range(salesmen)],
        target=len(problem.costs)
    )
    for _ in range(size):
        perm = list(range(len(problem.costs)))
        random.shuffle(perm)
        shares = cr.next()
        print(f'shares: {shares}')
        index = 0
        phenotype = []
        for share in shares:
            salesman = Tour(depot=random.sample(problem.depots, 1)[
                            0])  # currently only single starting depot :(
            salesman.tour = perm[index:index+share]
            if False:  # two opt?
                salesman.tour = two_opt(problem, salesman.tour)
            if False:  # choose best depot per salesman?
                salesman.tour, salesman.depot = rotate_depots(
                    problem, salesman.tour)
            phenotype.append(salesman)
            index += share
        solution = Solution(phenotype=phenotype)
        print(f'solution: {solution}')
        eval_fitness(problem, solution)
        print(f'solution with fitness: {solution}')
        assert index == len(problem.costs)
        pop.individuals.append(solution)
    return pop


def two_opt(problem: Problem, tour: list[int]) -> list[int]:
    n = len(tour)
    cur_len = tour_length(problem, None, tour, with_home=False)
    found_improvement = True
    while found_improvement:
        found_improvement = False
        for i in range(n - 1):
            for j in range(i + 1, n):
                c1 = tour[i]
                c2 = tour[j]
                c3 = tour[(i + 1) % n]
                c4 = tour[(j + 1) % n]
                a = -problem.costs[c1][c3]  # -dist(c1, c3)
                b = -problem.costs[c2][c4]  # -dist(c2, c4)
                c = problem.costs[c1][c2]  # dist(c1, c2)
                d = problem.costs[c3][c4]  # dist(c3, c4)
                len_delta = a + b + c + d
                if len_delta < -1e-4:
                    # reverse order of segment from i to j
                    tour[i+1:j+1] = reversed(tour[i+1:j+1])
                    cur_len += len_delta
                    found_improvement = True
    return tour

# rotates start and end of tour to match the closest depot


def rotate_depots(problem: Problem, tour: list[int]) -> tuple[list[int], int]:
    best_depot = problem.depots[0]
    for depot in problem.depots:
        for node in tour:
            ...
    return tour, random.sample(problem.depots, 1)[0]


def eval_fitness(problem: Problem, solution: Solution) -> Fitness:
    if solution.fitness:
        return
    fitness = Fitness(max_tour_length=(-sys.maxsize - 1), total_length=0)
    for salesman in solution.phenotype:
        tour_len = tour_length(problem, salesman.depot, salesman.tour)
        fitness.total_length += tour_len
        if tour_len > fitness.max_tour_length:
            fitness.max_tour_length = tour_len
    solution.fitness = fitness
    return fitness


def tour_length(problem: Problem, home: int, path: list[int], with_home=True):
    if with_home:
        path = [home, *path, home]
    total = 0
    for i in range(len(path) - 1):
        total += problem.costs[path[i]][path[i+1]]
    return total


def rotate_population(population: Population):
    ...
