use std::path::{Path, PathBuf};

use chrono::NaiveDate;
use plotly::{Bar, ImageFormat, Layout, Plot};
use plotly::common::{ErrorData, ErrorType, Title};
use plotly::layout::{Axis, AxisType, BarMode};

use crate::input::{Benchmarks, DeviceBenchmarks};

mod input;

struct PlotSave {
    bench: String,
    date: NaiveDate,
    original_path: PathBuf,
    plot: Plot,
}

fn plot_device_benchmark(benchmarks: Benchmarks, by_device: DeviceBenchmarks) -> PlotSave {
    let traces: Vec<Box<Bar<String, f64>>> = by_device.device_benchmarks.iter().map(
        |device_benchmarks|
            Bar::new(device_benchmarks.benchmarks.iter().map(|result| result.benchmark.clone()).collect(),
                     device_benchmarks.benchmarks.iter().map(|result| result.score).collect())
                .name(String::from(device_benchmarks.device.clone()).as_str())
                .error_y(ErrorData::new(ErrorType::Data).array(
                    device_benchmarks.benchmarks.iter()
                        .map(|result| result.error)
                        .collect()))
    ).collect();
    let layout = Layout::new()
        .y_axis(Axis::new().type_(AxisType::Log).title(Title::new(by_device.unit.as_str())))
        .title(Title::new(String::from(benchmarks.clone()).as_str()))
        .bar_mode(BarMode::Group)
        .x_axis(Axis::new().title(Title::new(by_device.date.to_string().as_str())));

    let mut plot = Plot::new();
    plot.set_layout(layout);
    for trace in traces {
        plot.add_trace(trace);
    }
    return PlotSave {
        bench: String::from(benchmarks),
        date: by_device.date,
        plot,
        original_path: by_device.original_path,
    };
}


fn main() {
    let benchmarks_path = "../bench-results/";
    let bench_collector = input::build_from_dir(benchmarks_path).unwrap();
    let mut result = Vec::with_capacity(bench_collector.benchmarks_by_device.len());
    for pairs in bench_collector.benchmarks_by_device {
        result.push(plot_device_benchmark(pairs.0, pairs.1));
    }
    result.iter().for_each(|plot| {
        let format = format!(
            "{}/{}-{}",
            plot.original_path.to_string_lossy(),
            plot.bench.replace(' ', "_"),
            plot.date.format("%Y-%m-%d"));
        plot.plot.save(Path::new(format.as_str()),
                       ImageFormat::PNG, 2560, 1440, 1.)
    })
}