import glob
import json
import os
import signal
import textwrap
from pathlib import Path
from pprint import pprint

import click
import numpy as np
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
        print(front)
        xs, ys = zip(*front)
        ax.plot(xs, ys, linestyle="-", marker="o", label=f"Front {i}")
    pdf.savefig(fig)
    plt.close()
    pdf.close()
    print(f'Saved to {out_file}')


# draw box plot for range of parameter values (such as mutation probability)
# arg #1: target param for box-spread
# arg #2: pattern of files to include in plot (solution files)
@cli.command('box')
@click.argument('parameter', metavar='parameter_to_plot')
@click.argument('glob_pat', metavar='glob_pattern')
def plot_boxes(parameter, glob_pat):
    # make data:
    paths = glob.glob(glob_pat, recursive=True)
    jsons = []
    for path in paths:
        if not os.path.isfile(path) or not os.path.splitext(path)[1] == '.json':
            continue
        jsons.append(path)
    data = {
        'Max Tour Length': {},
        'Total Length': {},
    }
    for j in jsons:
        with open(j, 'r') as f:
            run = json.load(f)
        seed = run['state']['seed']
        param_val = run['config'][parameter]
        if param_val not in data['Max Tour Length']:
            data['Max Tour Length'][param_val] = []
        if param_val not in data['Total Length']:
            data['Total Length'][param_val] = []
        print(f'seed {seed}')
        print(f'param val  {param_val}')
        for sol in run['state']['population']['paretto_front']:
            data['Max Tour Length'][param_val].append(-sol['fitness']['max_tour_length'])
            data['Total Length'][param_val].append(-sol['fitness']['total_length'])

    pprint({k: sorted(v.keys()) for k, v in data.items()})
    fig, ax = plt.subplots(1, 1)
    ax.set_xlabel('Mutation Probability')
    ax.set_ylabel('Fitness Value')
    bps = []
    for (k, v), color in zip(data.items(), ['red', 'blue']):
        bins = sorted(v.keys())
        bin_vals = [v[bin_] for bin_ in bins]
        # pprint(np.asarray(bins).shape)
        ax.set_title(k)
        spacing = sum(y - x for x, y in zip(bins, bins[1:])) / (len(bins) - 1)
        ax.set(xticks=bins, xlim=(bins[0] - spacing, bins[-1] + spacing))
        ax.tick_params(axis='x', labelsize=8)
        # ax[0, 0].set(xticks=range(1, 31), yticks=range(1, 31))
        bp = ax.boxplot(
            bin_vals,
            positions=bins,
            widths=spacing / 2,
            patch_artist=True,
            showmeans=True,
            showfliers=True,
            meanprops={
                'marker': None,
                'markeredgecolor': color,
                'markersize': 6,
            },
            medianprops={
                "color": color,
                "linewidth": 1,
            },
            boxprops={
                "facecolor": "#0000",
                "edgecolor": color,
                "linewidth": 1,
            },
            whiskerprops={
                "color": color,
                "linewidth": 1,
            },
            flierprops={
                'marker': 'o',
                'markerfacecolor': '#0000',
                'markeredgecolor': color,
                'markersize': 4
            },
            capprops={
                "color": color,
                "linewidth": 1
            },
        )
        bps.append(bp)
        ax.plot(bins, [sum(bv) / len(bv) for bv in bin_vals], color=color, marker=None, linewidth=1)
    ax.legend([bp['boxes'][0] for bp in bps], data.keys())
    ax.grid(color='#aaa', linestyle='--', linewidth=0.5)
    plt.show()
    #np.random.seed(10)
    #D = np.random.normal((3, 5, 4), (1.25, 1.00, 1.25), (100, 3))
    #print(D)

    # plot



# plot mean convergence for varying parameter
def plot_converg_speed(parameter, glob_pat, ):
    ...


def main():
    cli()


if __name__ == '__main__':
    cli()
