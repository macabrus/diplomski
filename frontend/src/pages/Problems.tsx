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
        return [{}]
    })
    return <>
        <Link class="btn btn-success" href="/problem/new">Create</Link>
        <For each={problems()}>{(problem, index) =>
            <></>
        }</For>
    </>
}
export default Problems;