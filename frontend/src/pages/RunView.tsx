import { useParams } from "@solidjs/router";
import { Chart, registerables } from "chart.js";
import "chartjs-adapter-date-fns";
import { Component, createResource, onCleanup, onMount } from "solid-js";

const RunView: Component = () => {
  const id = +useParams().id;
  /* Fetch detailed last snapshot of current run,
     note that datapoints which were emitted before will not be */
  createResource(async() => {
    const res = await fetch(`/api/run/${id}`);
    const data = await res.json();
    return data;
  });
  console.log(id);
  let chartRef: any;
  let ws: WebSocket;
  const endDate = new Date();
  endDate.setTime(endDate.getTime() + 3600); // 1 hour
  const data = {
    datasets: [
      {
        label: "Fitness",
        // cubicInterpolation: true,
        // tension: 0.1,
        // fill: true,
        data: [
          // {
          //   x: "2023-03-21 15:09:01",
          //   y: 10,
          // },
          // {
          //   x: "2023-03-21 15:09:30",
          //   y: 20,
          // },
          // {
          //   x: "2023-03-21 15:09:31",
          //   y: 30,
          // },
          // {
          //   x: "2023-03-21 15:10:31",
          //   y: 30,
          // },
          // {
          //   x: "2023-03-22 15:10:31",
          //   y: 40,
          // },
        ],
      },
    ],
  };
  onMount(() => {
    ws = new WebSocket('wss://127.0.0.1/api/stream');
    /* Subscribe to topic of current run's ID */
    ws.onopen = () => ws.send(JSON.stringify({
      type: 'sub',
      run_id: -1,
    }));
    console.log(data);
    Chart.register(...registerables);
    const chart = new Chart(chartRef, {
      type: "line",
      data: data,
      options: {
        interaction: { intersect: false },
        responsive: true,
        plugins: {
          legend: {
            position: "top",
          },
          title: {
            display: true,
            text: "Fitness Chart",
          },
        },
        showLine: true,
        scales: {
          y: {
            beginAtZero: true,
            // max: 100
          },
          x: {
            type: "time",
            time: {
              unit: "second",
            },
            ticks: {
              maxTicksLimit: 10,
              callback(value) {
                return new Date(value).toLocaleTimeString('hr-HR', {second: 'numeric', minute: 'numeric', hour: 'numeric'}); 
              },
            },
          },
        },
        elements: {
          point: {
            radius: 0,
          },
        },
      },
    });
    ws.onmessage = (message) => handleMessage(chart, JSON.parse(message.data));
    console.log(chart);
    // setInterval(() => handleData(chart, {}), 500);
  });

  onCleanup(() => {
    ws.close();
    console.log("Cleaning up (unsubscribing) websockets!");
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

function dates(config: any) {
  config = config || {};
  let start = config.start?.getTime() || new Date().getTime();
  let end;
  let step = config.step || 1000; // 1 second is default
  if (config.end) {
    end = config.end?.getTime();
  } else {
    end = new Date(start);
    end.setDate(end.getDate() + 1);
  }
  let i = start;
  let dates = [];
  while (i <= end) {
    dates.push(new Date(i));
    i += Math.random() * 10_000;
  }
  return dates;
}

function handleData(chart: Chart, message: any) {
  /* Add new Data */
  const data = chart.data;
  if (data.datasets.length > 0) {
    console.log("append");
    data.labels = months({ count: data.labels.length + 1 });

    for (let index = 0; index < data.datasets.length; ++index) {
      data.datasets[index].data.push(rand(-100, 100));
    }
  }

  /* Truncate tail of current data */
  chart.data.labels?.shift(); // remove the label first
  chart.data.datasets.forEach((dataset) => {
    dataset.data.shift();
  });
  chart.update();
}

function handleMessage(chart: Chart, message: any) {
  chart.data.datasets[0].data.push(message.value);
  console.log(message);
  chart.update();
}

// function messageHandler(message: any) {
//   switch (message.type) {
//     case "update": {
//       for (let chart of charts) {
//         let ds = chart.data.datasets.find(dataset => dataset.label === message.stat);
//         ds.push({ y: message.value, message. });

//       }
//       handleData(message);
//       break;
//     }
//     case "":
//       break;
//   }
// }

export default RunView;
