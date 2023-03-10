import { Component, lazy } from "solid-js";

import logo from "./logo.svg";
import styles from "./App.module.css";
import { Routes, Route, Link, useLocation } from "@solidjs/router";

const Home = lazy(() => import("./pages/Home"));
const Problems = lazy(() => import("./pages/Problems"));
const ProblemForm = lazy(() => import("./pages/ProblemForm"));
const Populations = lazy(() => import("./pages/PopulationList"));
const PopulationForm = lazy(() => import("./pages/PopulationForm"));

const App: Component = () => {
  function hl(path: string) {
    return useLocation().pathname === path ? "fw-bold" : "";
  }
  return (
    <>
      <Link href="/problem/new"></Link>
      <div class="nav-scroller bg-white shadow-sm">
        <nav
          class="navbar navbar-expand-lg nav-underline"
          aria-label="Secondary navigation"
        >
          <Link class="navbar-brand" href="/">
            <img src={logo} width="36" height="36" />
            Genetic Algorithm Platform
          </Link>
          <Link class="nav-link" href="/problem">
            Problems
          </Link>
          <Link class="nav-link" href="/population">
            Populations
          </Link>
          <Link class="nav-link" href="/run">
            Runs
          </Link>
          <Link class="nav-link" href="/archive">
            Archive
          </Link>
        </nav>
      </div>
      <main role="main" class="container">
        <Routes>
          <Route path="/" component={Home} />
          <Route path="/problem" component={Problems} />
          <Route path="/problem/new" component={ProblemForm} />
          <Route path="/population" component={Populations} />
          <Route path="/population/new" component={PopulationForm} />
          <Route path="/archive" element={<div>Archive of old runs</div>} />
        </Routes>
      </main>
    </>
  );
};

export default App;
