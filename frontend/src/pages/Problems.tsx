import { Link } from "@solidjs/router"
import { Component, createResource, createSignal, For } from "solid-js"

interface Problem {
    label: string
    dateAdded: string
    // date
}

const Problems: Component = () => {
    const [problems, setProblems] = createSignal([]);
    createResource(async () => {
        const res = await fetch('/api/problem', {
            method: 'GET'
        });
        setProblems(await res.json())
    })
    return <>
        <Link class="btn btn-success" href="/problem/new">Create</Link>
        <For each={problems()}>{(problem, index) =>
            JSON.stringify(problem)
        }</For>
    </>
}
export default Problems;