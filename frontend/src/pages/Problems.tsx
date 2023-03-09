import { Component, createResource } from "solid-js"

interface Problem {
    label: string
    dateAdded: string
    // date
}

const Problems: Component = () => {
    const problems = createResource(async () => {
        return [{}]
    })
    return <>
        
    </>
}
export default Problems;