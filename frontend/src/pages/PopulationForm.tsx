import { Component, createMemo, createResource, createSignal, For } from "solid-js";
import { Problem } from "../models/problem";

import {model, numModel} from "./forms";


const [label, setLabel] = createSignal("");
const [size, setSize] = createSignal(30);
const [problemId, setProblemId] = createSignal<number>(-1);
const [strategy, setStrategy] = createSignal("");
const [problem, setProblem] = createSignal(null);


const PopulationForm: Component = () => {
  const [problems, setProblems] = createSignal<Problem[]>([]);
  createResource(async () => {
    const res = await fetch('/api/problem');
    setProblems(await res.json());
  });
  const form = createMemo(() => {
    const form = {
      label: label(),
      problem_id: problemId(),
      size: size()
    };
    console.log(form);
    return form;
  });

  async function submit() {
    await fetch('/api/population', {
      method: 'POST',
      body: JSON.stringify(form())
    });
  }
  return (
    <form class="rounded-lg m-5 p-3 shadow-sm bg-white">
      <input
        type="text"
        use:model={[label, setLabel]}
        class="form-control"
        id="label"
        aria-describedby="label-help"
      />
      <input
        type="range"
        use:numModel={[size, setSize]}
        class="form-control"
        id="size"
        aria-describedby="size-help"
        min="10" max="1000" step="10"
      />
      <select class="custom-select" use:numModel={[problemId, setProblemId]}>
        <option selected disabled>Select Problem</option>
        <For each={problems()}>{
          (problem) =>
            <option value={problem.id}>{problem.label}</option>
        }</For>
      </select>
      <div id="size-help" class="form-text">
        Optional (derived from file)
      </div>
      <button onclick={submit} class="btn btn-primary" type="button">Submit</button>
    </form>
  );
};

export default PopulationForm;

const _ = {
  model, numModel
}