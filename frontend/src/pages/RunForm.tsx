import {
  Component,
  createMemo,
  createResource,
  createSignal,
  For,
} from "solid-js";
import { model, numModel } from "../lib/forms";
import { Population } from "../models/problem";

const [label, setLabel] = createSignal("");
const [popId, setPopId] = createSignal(-1);
const [mutProb, setMutProb] = createSignal(0.05);
const [sharingFreq, setSharingFreq] = createSignal(0.05);
const [sharingDist, setSharingDist] = createSignal(300);
const [ignoreRankProbability, setIgnoreRankProbability] = createSignal(0.05);
const [maxIter, setMaxIter] = createSignal(1000);
const [maxSteadyGen, setMaxSteadyGen] = createSignal(1000);

/* dummy select option */
const dummy = {
  id: -1,
  selected: true,
  disabled: true,
  label: "Select Population",
};

const RunForm: Component = () => {
  const [populations] = createResource(async () => {
    const res = await fetch("/api/population?format=short");
    const data = [dummy, ...(await res.json())];
    /* If population was removed in the meantime, reset it to -1 */
    data.forEach((pop) => {
      if (pop.id === popId()) {
        pop.selected = true;
      } else {
        pop.selected = false;
      }
    });
    return data;
  });

  const form = createMemo(() => {
    const form = {
      label: label(),
      population_id: popId(),
      mutation_probability: mutProb(),
      sharing_distance: sharingDist(),
      sharing_frequency: sharingFreq(),
      ignore_rank_probability: ignoreRankProbability(),
      max_iter: maxIter(),
      max_steady_generations: maxSteadyGen()
    };
    console.log(form);
    return form;
  });

  async function submit() {
    await fetch("/api/run", {
      method: "POST",
      body: JSON.stringify(form()),
    });
  }

  return (
    <form class="p-3 rounded shadow-sm bg-white">
      {/* Label */}
      <div class="col-auto mb-3">
        <label for="label" class="form-label">
          Label
        </label>
        <input
          type="text"
          use:model={[label, setLabel]}
          class="form-control"
          id="label"
          aria-describedby="label-help"
        />
        <div id="label-help" class="form-text">
          Optional
        </div>
      </div>

      {/* SELECT POPULATION TO USE IN THIS RUN (IMPLIES A PROBLEM) */}
      <select class="form-select" use:numModel={[popId, setPopId]}>
        <For each={populations()}>
          {(pop) => (
            <option
              selected={pop.selected}
              disabled={pop.disabled}
              value={pop.id}>
              {pop.label}
            </option>
          )}
        </For>
      </select>

      {/* MUTATION PROBABILITY */}
      <div class="mb-3">
        <label for="customRange1" class="form-label">
          Mutation probability:
        </label>
        <input
          type="range"
          use:numModel={[mutProb, setMutProb]}
          class="form-range"
          id="size"
          aria-describedby="size-help"
          min="0"
          max="1"
          step="0.001"></input>
        <div id="two-opt-help" class="form-text">
          {mutProb()}
        </div>
      </div>

      {/* SHARING DISTANCE */}
      <div class="mb-3">
        <label for="customRange1" class="form-label">
          Sharing distance:
        </label>
        <input
          type="range"
          use:numModel={[sharingDist, setSharingDist]}
          class="form-range"
          id="size"
          aria-describedby="size-help"
          min="0"
          max="100000"
          step="10"></input>
        <div id="two-opt-help" class="form-text">
          {sharingDist()}
        </div>
      </div>

      {/* SHARING FREQUENCY */}
      <div class="mb-3">
        <label for="customRange1" class="form-label">
          Sharing frequency:
        </label>
        <input
          type="range"
          use:numModel={[sharingFreq, setSharingFreq]}
          class="form-range"
          id="size"
          aria-describedby="size-help"
          min="0"
          max="1"
          step="0.001"></input>
        <div id="two-opt-help" class="form-text">
          {sharingFreq()}
        </div>
      </div>

      {/* RANK IGNORING PROBABILITY */}
      <div class="mb-3">
        <label for="customRange1" class="form-label">
          Probability for ignoring non-dominated sorting rank in tournament selection:
        </label>
        <input
          type="range"
          use:numModel={[ignoreRankProbability, setIgnoreRankProbability]}
          class="form-range"
          id="size"
          aria-describedby="size-help"
          min="0"
          max="1"
          step="0.001"></input>
        <div id="two-opt-help" class="form-text">
          {ignoreRankProbability()}
        </div>
      </div>

      {/* MAX ITER? */}
      <div class="mb-3">
        <label for="customRange1" class="form-label">
          Maximum Number of Iterations:
        </label>
        <input
          type="range"
          use:numModel={[maxIter, setMaxIter]}
          class="form-range"
          id="size"
          aria-describedby="size-help"
          min="1"
          max="1000000"
          step="1"></input>
        <div id="two-opt-help" class="form-text">
          {maxIter().toLocaleString()}
        </div>
      </div>

      {/* MAX STEADY GENERATIONS? */}
      <div class="mb-3">
        <label for="customRange1" class="form-label">
          Maximum Number of Steady Generations before Termination
        </label>
        <input
          type="range"
          use:numModel={[maxSteadyGen, setMaxSteadyGen]}
          class="form-range"
          id="size"
          aria-describedby="size-help"
          min="1"
          max="1000"
          step="1"></input>
        <div id="two-opt-help" class="form-text">
          {maxSteadyGen().toLocaleString()}
        </div>
      </div>

      <h3>Run configuration</h3>
      <ul>
        <li>Mutation probability</li>
        <li>Sharing probability</li>
        <li>Max Iterations</li>
        <li>Max Steady Iterations</li>
        <li>Target Fitness</li>
      </ul>

      <div class="row justify-content-center">
        <button
          disabled={popId() === -1}
          onclick={submit}
          class="btn btn-primary"
          type="button">
          Submit
        </button>
      </div>
    </form>
  );
};

export default RunForm;

const _ = {
  model,
  numModel,
};
