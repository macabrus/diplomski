import { useParams } from "@solidjs/router";
import { Chart, registerables } from "chart.js";
import "chartjs-adapter-date-fns";
import { Component, onMount } from "solid-js";

const RunView: Component = () => {
  let chartRef: any;
  // let ws = new WebSocket('ws://socket.server.com');
  // ws.onmessage = (message) => {
  //   messageHandler(JSON.parse(message.data));
  // }
  const data = {
    labels: months({ count: 100 }),
    datasets: [
      {
        label: "Metric 1",
        data: numbers({ count: 100, min: -100, max: 100 }),
        borderColor: CHART_COLORS.red,
        backgroundColor: transparentize(CHART_COLORS.red, 0.5),
      },
      {
        label: "Metric 2",
        data: numbers({ count: 100, min: -100, max: 100 }),
        borderColor: CHART_COLORS.blue,
        backgroundColor: transparentize(CHART_COLORS.blue, 0.5),
      },
    ],
  };
  onMount(() => {
    console.log(data);
    Chart.register(...registerables);
    const chart = new Chart(chartRef, {
      type: 'line',
      data: data,
      options: {
        interaction: { intersect: false },
        responsive: true,
        plugins: {
          legend: {
            position: 'top',
          },
          title: {
            display: true,
            text: 'Chart.js Line Chart'
          }
        }
      },
    });
    console.log(chart);
    setInterval(() => handleData(chart, {}), 500);
  });

  const params = useParams();
  return (
    <>
      <h3>/run/{params.id}</h3>
      <div style="width: 800px;">
        <canvas ref={chartRef}></canvas>
      </div>
    </>
  );
};

let _seed = 0;

function srand(seed: any) {
  _seed = seed;
}

function rand(min?: number, max?: number) {
  min = min || 0;
  max = max || 0;
  _seed = (_seed * 9301 + 49297) % 233280;
  return min + (_seed / 233280) * (max - min);
}

function numbers(config: any) {
  var cfg = config || {};
  var min = cfg.min || 0;
  var max = cfg.max || 100;
  var from = cfg.from || [];
  var count = cfg.count || 8;
  var decimals = cfg.decimals || 8;
  var continuity = cfg.continuity || 1;
  var dfactor = Math.pow(10, decimals) || 0;
  var data = [];
  var i, value;

  for (i = 0; i < count; ++i) {
    value = (from[i] || 0) + rand(min, max);
    if (rand() <= continuity) {
      data.push(Math.round(dfactor * value) / dfactor);
    } else {
      data.push(null);
    }
  }

  return data;
}

function parseColor(input: string) {
  return input
    .split("(")[1]
    .split(")")[0]
    .split(",")
    .map((col) => +col);
}

function transparentize(value: string, opacity: number) {
  return "rgb(" + parseColor(value).join(", ") + ", " + opacity + ")";
}

export const CHART_COLORS = {
  red: "rgb(255, 99, 132)",
  orange: "rgb(255, 159, 64)",
  yellow: "rgb(255, 205, 86)",
  green: "rgb(75, 192, 192)",
  blue: "rgb(54, 162, 235)",
  purple: "rgb(153, 102, 255)",
  grey: "rgb(201, 203, 207)",
};

const MONTHS = [
  "January",
  "February",
  "March",
  "April",
  "May",
  "June",
  "July",
  "August",
  "September",
  "October",
  "November",
  "December",
];

function months(config: any) {
  var cfg = config || {};
  var count = cfg.count || 12;
  var section = cfg.section;
  var values = [];
  var i, value;

  for (i = 0; i < count; ++i) {
    value = MONTHS[Math.ceil(i) % 12];
    values.push(value.substring(0, section));
  }

  return values;
}

function handleData(chart: Chart, message: any) {
  /* Add new Data */
  const data = chart.data;
  if (data.datasets.length > 0) {
    data.labels = months({count: data.labels.length + 1});

    for (let index = 0; index < data.datasets.length; ++index) {
      data.datasets[index].data.push(rand(-100, 100));
    }
  }

  /* Truncate tail of current data */
  chart.data.labels?.shift(); // remove the label first
  chart.data.datasets.forEach(dataset => {
    dataset.data.shift();
  });
  chart.update();
}

function messageHandler(message: any) {
  switch (message.type) {
    case "update": {
      handleData(message);
      break;
    }
    case "":
      break;
  }
}

export default RunView;
