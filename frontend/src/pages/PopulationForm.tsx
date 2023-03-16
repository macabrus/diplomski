import {
  Component,
  createMemo,
  createResource,
  createSignal,
  For,
} from "solid-js";
import { Problem } from "../models/problem";

import { model, numModel, boolModel } from "../lib/forms";

const [label, setLabel] = createSignal("");
const [numSalesmen, setNumSalsesmen] = createSignal(1);
const [size, setSize] = createSignal(30);
const [problemId, setProblemId] = createSignal<number>(-1);
const [twoOpt, setTwoOpt] = createSignal(false);
const [rotate, setRotate] = createSignal(false);

const PopulationForm: Component = () => {
  const [problems, setProblems] = createSignal<Problem[]>([]);
  createResource(async () => {
    const res = await fetch("/api/problem");
    setProblems(await res.json());
  });
  const form = createMemo(() => {
    const form = {
      label: label(),
      num_salesmen: numSalesmen(),
      problem_id: problemId(),
      size: size(),
      two_opt: twoOpt(),
      rotate: rotate(),
    };
    console.log(form);
    return form;
  });

  async function submit() {
    await fetch("/api/population", {
      method: "POST",
      body: JSON.stringify(form()),
    });
  }
  return (
    <form class="rounded-lg m-5 p-3 shadow-sm bg-white">
      <div class="mb-3">
        <label for="exampleFormControlInput1" class="form-label">
          Label:
        </label>
        <input
          type="text"
          use:model={[label, setLabel]}
          class="form-control"
          id="label"
          aria-describedby="label-help"
        />
      </div>
      <div class="mb-3">
        <label for="num-salesmen-input" class="form-label">
          Number of Salesmen:
        </label>
        <input
          type="text"
          use:numModel={[numSalesmen, ((val: any) => {}) as any]}
          class="form-control"
          aria-describedby="label-help"
        />
      </div>
      
      <div class="mb-3">
        <input
          type="range"
          use:numModel={[numSalesmen, setNumSalsesmen]}
          class="form-range"
          id="num-salesmen-input"
          aria-describedby="size-help"
          min="1"
          max="20"
          step="1"></input>
        <div id="two-opt-help" class="form-text">
          {size()}
        </div>
      </div>

      <label for="customRange1" class="form-label">
        Population Size:
      </label>
      <div class="mb-3">
        <input
          type="range"
          use:numModel={[size, setSize]}
          class="form-range"
          id="size"
          aria-describedby="size-help"
          min="10"
          max="1000"
          step="10"></input>
        <div id="two-opt-help" class="form-text">
          {size()}
        </div>
      </div>

      <select class="form-select" use:numModel={[problemId, setProblemId]}>
        <option selected disabled>
          Select Problem
        </option>
        <For each={problems()}>
          {(problem) => <option value={problem.id}>{problem.label}</option>}
        </For>
      </select>
      <div class="form-check">
        <input
          class="form-check-input"
          type="checkbox"
          use:boolModel={[twoOpt, setTwoOpt]}
          id="two-opt-check"
          aria-describedby="two-opt-help"
        />
        <label class="form-check-label" for="flexCheckChecked">
          2-Opt
        </label>
        <div id="two-opt-help" class="form-text">
          Use 2-opt optimization on randomly generated individuals
        </div>
      </div>
      <div class="form-check">
        <input
          class="form-check-input"
          type="checkbox"
          use:boolModel={[rotate, setRotate]}
          id="rotate-check"
          aria-describedby="rotate-help"
        />
        <label class="form-check-label" for="flexCheckChecked">
          Rotate
        </label>
        <div id="two-opt-help" class="form-text">
          Align each tour to start and end in a depot closest to home depot
        </div>
      </div>

      <button
        disabled={problemId() === -1 || !label()}
        onclick={submit}
        class="btn btn-primary"
        type="button">
        Submit
      </button>
    </form>
  );
};

export default PopulationForm;

const _ = {
  model,
  numModel,
  boolModel,
};
