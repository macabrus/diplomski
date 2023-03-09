import { Component, lazy } from 'solid-js';

import logo from './logo.svg';
import styles from './App.module.css';
import { Routes, Route, Link } from '@solidjs/router';


const Problems = lazy(() => import("./pages/Problems"));
const ProblemForm = lazy(() => import("./pages/ProblemForm"));
const Populations = lazy(() => import("./pages/PopulationList"));
const PopulationForm = lazy(() => import("./pages/PopulationForm"));

const App: Component = () => {
  return (
    <>
      <div class="nav-scroller bg-white shadow-sm">
        <nav class="navbar navbar-expand-lg nav-underline" aria-label="Secondary navigation">
          <a class="navbar-brand" href="#">Genetic Algorithm Platform</a>
          <Link class="nav-link active" href="/problem">Problems</Link>
          <Link class="nav-link" href="/population">Populations</Link>
          <Link class="nav-link" href="/run">Runs</Link>
        </nav>
      </div>
      <main role="main" class="container">
        <Routes>
          <Route path="/problem" component={Problems} />
          <Route path="/problem/new" component={ProblemForm} />
          <Route path="/population" component={Populations} />
          <Route path="/population/new" component={PopulationForm} />
          <Route path="/statistics" element={<div>Archive of old runs</div>} />
        </Routes>
      </main>
    </>
  );
};

export default App;
