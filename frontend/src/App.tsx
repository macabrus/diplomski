import { Component, lazy } from "solid-js";

import logo from "./logo.svg";
import styles from "./App.module.css";
import { Routes, Route, Link, useLocation } from "@solidjs/router";

const Home = lazy(() => import("./pages/Home"));
const Problems = lazy(() => import("./pages/ProblemList"));
const RunList = lazy(() => import("./pages/RunList"));
const RunForm = lazy(() => import("./pages/RunForm"));
const ProblemForm = lazy(() => import("./pages/ProblemForm"));
const Populations = lazy(() => import("./pages/PopulationList"));
const PopulationForm = lazy(() => import("./pages/PopulationForm"));
const WorkerList = lazy(() => import("./pages/WorkerList"));

const App: Component = () => {
  function hl(path: string) {
    return useLocation().pathname === path ? "fw-bold" : "";
  }
  return (
    <>
      <Link href="/problem/new"></Link>
      <nav class="navbar navbar-expand-lg navbar-light bg-light">
        <div class="container-fluid">
          <Link class="navbar-brand" href="/">
            <img src={logo} width="36" height="36" />
            Genetic Algorithm Platform
          </Link>
          <button
            class="navbar-toggler"
            type="button"
            data-bs-toggle="collapse"
            data-bs-target="#navbarNav"
            aria-controls="navbarNav"
            aria-expanded="false"
            aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
          </button>
          <div class="collapse navbar-collapse" id="navbarNav">
            <ul class="navbar-nav">
              <li class="nav-item">
                <Link class="nav-link" href="/problem">
                  Problems
                </Link>
              </li>
              <li class="nav-item">
                <Link class="nav-link" href="/population">
                  Populations
                </Link>
              </li>
              <li class="nav-item">
                <Link class="nav-link" href="/run">
                  Runs
                </Link>
              </li>
              <li class="nav-item">
                <Link class="nav-link" href="/worker">
                  Workers
                </Link>
              </li>
              <li class="nav-item">
                <Link class="nav-link" href="/archive">
                  Archive
                </Link>
              </li>
            </ul>
          </div>
        </div>
      </nav>
      <main role="main" class="container">
        <Routes>
          <Route path="/" component={Home} />
          <Route path="/problem" component={Problems} />
          <Route path="/problem/new" component={ProblemForm} />
          <Route path="/population" component={Populations} />
          <Route path="/population/new" component={PopulationForm} />
          <Route path="/run" component={RunList} />
          <Route path="/run/new" component={RunForm} />
          <Route path="/worker" component={WorkerList} />
          <Route path="/archive" element={<div>Archive of old runs</div>} />
        </Routes>
      </main>
    </>
  );
};

export default App;
