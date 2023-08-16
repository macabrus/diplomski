import glob
import json
import os
import signal
import textwrap
from pathlib import Path
from pprint import pprint

import click
from matplotlib import pyplot as plt
from matplotlib.backends.backend_pdf import PdfPages

signal.signal(signal.SIGINT, signal.SIG_DFL)


@click.group
def cli():
    pass


# file: name of file to load solution from
# name: name of metric to plot
# colors: specify colors to use per metric
# parameters: list of names of parameters to plot
@cli.command('metrics')
@click.argument('file')
@click.argument('metric_name', )
@click.option('-o', '--output', 'output')
def plot_metric(file, name, colors=None, parameters=None, output=None):
    with open(file, 'r') as f:
        json.loads(f.read())


@cli.command('solutions')
@click.argument('json_file', metavar='FILE')
def plot_solutions(json_file):
    base = os.path.basename(json_file)
    base, ext = os.path.splitext(base)
    with open(json_file, 'r') as f:
        data = json.load(f)
    display = data['problem']['display']
    depot_map = data['problem']['dummy_to_real_depot']
    pop = data['state']['population']['individuals']
    sols = []
    for individual in pop:
        sol_plot = []
        sol = {
            'max_tour_length': -individual['fitness']['max_tour_length'],
            'total_length': -individual['fitness']['total_length'],
            'plot': sol_plot,
        }
        for salesman in individual['phenotype']:
            plot = [display[str(depot_map[str(salesman['depot'])])]]  # starting node
            for node in salesman['tour']:
                plot.append(display[str(node)])
            plot.append(display[str(depot_map[str(salesman['depot'])])])  # ending node
            sol_plot.append(plot)
        sols.append(sol)
    # fig, axs = plt.subplots(10, 10)
    pdf = PdfPages(f'{base}.pdf')
    for i, sol in enumerate(sols):
        print(f'Plotting {i}')
        props = dict(boxstyle='round', facecolor='wheat', alpha=0.5)
        textbox = textwrap.dedent(f'''
            --- Solution {i} ---
            Longest Tour: {sol['max_tour_length']}
            Total Length: {sol['total_length']}
        ''')
        fig, ax = plt.subplots(1, 1)
        fig.text(
            0.05, 0.95, textbox,
            transform=ax.transAxes,
            fontsize=7,
            verticalalignment='top',
            bbox=props
        )
        for sal in sol['plot']:
            ax.plot(*zip(*sal), '-o')
        ax.grid()
        pdf.savefig(fig)
        plt.close()
    pdf.close()


@cli.command('fronts')
@click.argument('json_file', metavar='file')
@click.option('-o', '--output', 'out_file', default=None)
@click.option('-d', '--destination', 'dest_dir', default='output')
def plot_fronts(json_file, out_file=None, dest_dir=None):
    dest = Path(dest_dir)
    dest.mkdir(exist_ok=True, parents=True)
    if out_file is None:
        filename = os.path.basename(json_file)
        base, ext = os.path.splitext(filename)
        out_file = dest / Path(f'fronts-{base}.pdf')
    with open(json_file, 'r') as f:
        data = json.load(f)
    pop = data['state']['population']
    s = max(pop['individuals'], key=lambda s: s['rank'])
    fronts = [[] for _ in range(s['rank'] + 1)]
    variants = {}
    for sol in pop['individuals']:
        rank = sol['rank']
        max_tour_len = -sol['fitness']['max_tour_length']
        total_len = -sol['fitness']['total_length']
        if (max_tour_len, total_len) not in variants:
            variants[max_tour_len, total_len] = sol
        fronts[rank].append((max_tour_len, total_len))
    pprint(variants.keys())
    pdf = PdfPages(out_file)
    fig, ax = plt.subplots()
    ax.set_aspect('equal', 'box')
    ax.grid()
    for i, front in enumerate(fronts):
        front = sorted(front)
        xs, ys = zip(*front)
        ax.plot(xs, ys, linestyle="-", marker="o", label=f"Front {i}")
    pdf.savefig(fig)
    plt.close()
    pdf.close()


# draw box plot for range of parameter values (such as mutation probability)
# arg #1: target param for box-spread
# arg #2: pattern of files to include in plot (solution files)
@cli.command('box')
@click.argument('parameter', metavar='parameter_to_plot')
@click.argument('glob_pat', metavar='glob_pattern')
def plot_boxes(parameter, glob_pat):
    import numpy as np

    # make data:
    paths = glob.glob(glob_pat, recursive=True)
    jsons = []
    for path in paths:
        if not os.path.isfile(path) or not os.path.splitext(path)[1] == '.json':
            continue
        jsons.append(path)
    data = {
        'Max Tour Length': [[] for i in range(len(jsons))],
        'Total Length': [[] for i in range(len(jsons))],
    }
    for j in jsons:
        with open(j, 'r') as f:
            run = json.load(f)
        seed = run['state']['seed']
        mut_prob = run['config']['mutation_probability']
        print(f'seed {seed}')
        print(f'mut  {mut_prob}')
        for sol in run['state']['population']['paretto_front']:
            data['Max Tour Length'][seed].append(-sol['fitness']['max_tour_length'])
            data['Total Length'][seed].append(-sol['fitness']['total_length'])

    pprint(data)
    exit(0)
    np.random.seed(10)
    D = np.random.normal((3, 5, 4), (1.25, 1.00, 1.25), (100, 3))
    print(D)

    # plot
    fig, ax = plt.subplots()
    VP = ax.boxplot(
        D,
        positions=[1, 2, 3],
        widths=0.5,
        patch_artist=True,
        showmeans=True,
        showfliers=True,
        meanprops={
            'marker': 'x',
            'markeredgecolor': '#f00',
            'markersize': 6,
        },
        medianprops={
            "color": "red",
            "linewidth": 1,
        },
        boxprops={
            "facecolor": "#0000",
            "edgecolor": "red",
            "linewidth": 1,
        },
        whiskerprops={
            "color": "#f008",
            "linewidth": 1
        },
        flierprops={
            'marker': 'o',
            'markerfacecolor': '#0000',
            'markeredgecolor': '#f008',
            'markersize': 4
        },
        capprops={"color": "#f008", "linewidth": 1},
    )
    ax.grid(color='#aaa', linestyle='--', linewidth=0.5)
    ax.set(xlim=(0, 8), xticks=np.arange(1, 8),
           ylim=(0, 8), yticks=np.arange(1, 8))

    plt.show()


# plot mean convergence for varying parameter
def plot_converg_speed(parameter, glob_pat, ):
    ...


def main():
    cli()


if __name__ == '__main__':
    cli()
