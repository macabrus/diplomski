import { Link } from "@solidjs/router"
import { Component, createEffect, createResource, createSignal, For } from "solid-js"

interface Problem {
    id: number
    label: string
    description: string
    dateAdded: string
    // date
}

const Problems: Component = () => {
    const [problems, setProblems] = createSignal<Problem[]>([]);
    const [_, {refetch}] = createResource(async () => {
        const res = await fetch('/api/problem', {
            method: 'GET'
        });
        const json = await res.json();
        setProblems(json);
    })

    async function remove(id: number) {
        console.log(id);
        await fetch(`/api/problem/${id}`, {
            method: 'DELETE',
        });
        await refetch();
    }
    return <>
        <Link class="btn btn-success" href="/problem/new">Create</Link>
        <For each={problems()}>{(problem, index) =>
            <div class="rounded-lg m-5 p-3 shadow bg-white">
                <h2>{problem?.label}</h2>
                <p>{problem?.description}</p>
                {/* {JSON.stringify(problem)} */}
                <button class="btn btn-danger" onclick={() => remove(problem.id)}>Remove</button>
            </div>
        }</For>
    </>
}
export default Problems;