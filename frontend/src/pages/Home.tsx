import { Component } from "solid-js";

const Home: Component = () => {
    return <>
        <div class="rounded-lg m-5 p-3 shadow bg-white">
        <h1>About</h1>
        <p>Welcome to platform for benchmarking and analysing genetic algorithms.</p>
        <p> You can upload problem in any of supported formats (TSPLib) or write custom extension for new format.
            Create a population for given problem and execute evolution loop.
            While algorithm is running, you can monitor progress (fitness etc...)
            You can pause execution at any time.
            When stopping criteria is met, run becomes archived.
        </p>
</div>
    </>
}

export default Home;