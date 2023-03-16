import { Component, createResource, createSignal, For } from "solid-js";
import { numModel } from "../lib/forms";
import { Population } from "../models/problem";

const RunForm: Component = () => {
  const [popId, setPopId] = createSignal(-1);
  const [populations, setPopulations] = createSignal([] as Population[]);

  createResource(async () => {
    const res = await fetch('/api/population?format=short');
    setPopulations(await res.json());
  });

  return <form class="p-3 rounded shadow-sm bg-white">
    <select class="form-select" use:numModel={[popId, setPopId]}>
        <option selected disabled>
          Select Population
        </option>
        <For each={populations()}>
          {(pop) => <option value={pop.id}>{pop.label}</option>}
        </For>
      </select>
      <h3>Run configuration</h3>
      <ul>
        <li>Mutation probability</li>
        <li>Sharing probability</li>
        <li>Max Iterations</li>
        <li>Max Steady Iterations</li>
        <li>Target Fitness</li>
      </ul>
  </form>
}

export default RunForm;

const _ = {
  numModel
}