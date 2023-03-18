import { Link } from "@solidjs/router";
import { Component, createResource, For } from "solid-js";

const RunList: Component = () => {
  const [runs] = createResource(async () => {
    const res = await fetch("/api/run");
    const data = await res.json();
    return data;
  });
  return (
    <>
      <div>
        <Link href="/run/new" class="btn btn-success">
          Create
        </Link>
      </div>
      <div>
        <For each={runs()}>{(run) => <p>{JSON.stringify(run)}</p>}</For>
      </div>
    </>
  );
};

export default RunList;
