import { useNavigate } from "@solidjs/router";
import { Component, createMemo, createSignal, onMount } from "solid-js";
import { Problem } from "../models/problem";
import { model } from "./forms";


const [label, setLabel] = createSignal("");
const [color, setColor] = createSignal("");
const [description, setDescription] = createSignal("");
const [file, setFile] = createSignal("");


const ProblemForm: Component = () => {
  const m = model;

  const form = createMemo(() => {
    const form = {
      label: label(),
      color: color(),
      description: description(),
      file: file(),
    };
    console.log(form);
    return form;
  });

  async function submit(e: any) {
    const res = await fetch("/api/problem", {
      method: "POST",
      body: JSON.stringify(form()),
    });
    if (200 <= res.status && res.status < 300) {
      const data: Problem = await res.json();
      const navigate = useNavigate();
      navigate(`/problem/${data.id}`);
    }
  }
  return (
    <>
      <form class="rounded-lg m-5 p-3 shadow-sm bg-white">
        <div class="row">
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
              Optional (derived from file)
            </div>
          </div>

          {/* Symbolic color label */}
          <div class="col-auto mb-3">
            <label for="exampleColorInput" class="form-label">
              Symbolic Color
            </label>
            <input
              use:model={[color, setColor]}
              type="color"
              class="form-control form-control-color"
              id="exampleColorInput"
              value="#563d7c"
              title="Choose your color"
            />
          </div>
        </div>

        {/* Description */}
        <div class="mb-3">
          <label for="description" class="form-label">
            Problem Description
          </label>
          <input
            use:model={[description, setDescription]}
            class="form-control"
            id="description"
            aria-describedby="description-info"
          />
          <div id="description-info" class="form-text">
            Optional (extracted from file)
          </div>
        </div>

        {/* File */}
        <div class="mb-3">
          <label for="file" class="form-label">
            Problem Description
          </label>
          <input
            type="file"
            use:model={[file, setFile]}
            class="form-control"
            id="file"
            aria-describedby="file-info"
          />
          <div id="file-info" class="form-text">
            Problem description file ()
          </div>
        </div>
        <div class="row justify-content-center">
          <button onclick={submit} class="btn btn-primary" type="button">
            Submit
          </button>
        </div>
      </form>
    </>
  );
};

export default ProblemForm;
