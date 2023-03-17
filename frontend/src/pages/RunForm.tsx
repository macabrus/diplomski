import {
  Component,
  createMemo,
  createResource,
  createSignal,
  For,
} from "solid-js";
import { numModel } from "../lib/forms";
import { Population } from "../models/problem";

const [label, setLabel] = createSignal("");
const [popId, setPopId] = createSignal(-1);
const [mutProb, setMutProb] = createSignal("0.05");
const [sharingFreq, setSharingFreq] = createSignal("0.05");
// const []
const RunForm: Component = () => {
  const [populations, setPopulations] = createSignal([] as Population[]);
  createResource(async () => {
    const res = await fetch("/api/population?format=short");
    setPopulations(await res.json());
    /* If population was removed in the meantime, reset it to -1 */
    if (!populations().some((pop) => pop.id == popId())) {
      console.log("oops");
      setPopId(-1);
    }
  });

  const form = createMemo(() => {
    const form = {
      label: label(),
      population_id: popId(),
      mutation_probability: mutProb(),
      sharing_frequency: sharingFreq(),
    };
    console.log(form);
    return form;
  });

  async function submit() {
    await fetch('/api/run', {
      method: 'POST',
      body: JSON.stringify(form())
    });
  }

  return (
    <form class="p-3 rounded shadow-sm bg-white">
      {/* SELECT POPULATION TO USE IN THIS RUN (IMPLIES A PROBLEM) */}
      <select class="form-select" use:numModel={[popId, setPopId]}>
        <option value="-1" selected disabled>
          Select Population
        </option>
        <For each={populations()}>
          {(pop) => <option value={pop.id}>{pop.label}</option>}
        </For>
      </select>

      {/* MUTATION PROBABILITY */}
      <h3>Run configuration</h3>
      <ul>
        <li>Mutation probability</li>
        <li>Sharing probability</li>
        <li>Max Iterations</li>
        <li>Max Steady Iterations</li>
        <li>Target Fitness</li>
      </ul>

      <div class="row justify-content-center">
        <button onclick={submit} class="btn btn-primary" type="button">
          Submit
        </button>
      </div>
    </form>
  );
};

/* https://stackoverflow.com/a/175787 */
function isNumeric(str: any) {
  if (typeof str != "string") return false; // we only process strings!
  return (
    !isNaN(str) && // use type coercion to parse the _entirety_ of the string (`parseFloat` alone does not do this)...
    !isNaN(parseFloat(str))
  ); // ...and ensure strings of whitespace fail
}

export default RunForm;

const _ = {
  numModel,
};
