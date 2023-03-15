import { Link } from "@solidjs/router";
import { Component } from "solid-js"

const RunList: Component = () => {
    return <div><Link href="/run/new" class="btn btn-success">Create</Link></div>
}

export default RunList;