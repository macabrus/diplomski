import { Component, createResource, For } from "solid-js";

export interface Worker {
  ip: string
  port: string
  slots: number
  used: number
}

const WorkerList: Component = () => {
  const [workers] = createResource(async () => {
    const res = await fetch('/api/worker');
    const data = await res.json();
    return data;
  });


  return (
    <>
      <p>List of registered workers for running evolution loops</p>
      <For each={workers()}>{(worker) => 
        <p>{JSON.stringify(worker)}</p>
      }</For>
    </>
  );
};

export default WorkerList;
