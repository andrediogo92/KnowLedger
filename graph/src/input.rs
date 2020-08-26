use std::cmp::Ordering;
use std::error::Error;
use std::fmt;
use std::fs::File;
use std::io::BufReader;
use std::path::PathBuf;

use chrono::NaiveDate;
use serde::Deserialize;

const SORTING_PREFIX: &str = "sorting";
const SERIALIZATION_PREFIX: &str = "serialization";
const SIGNATURE_PREFIX: &str = "signature";
const COMBINED_PREFIX: &str = "combined";

#[derive(Debug, Clone)]
pub struct BenchmarkId {
    benchmark_type: Benchmarks,
    date: NaiveDate,
    device: String,
    original_path: PathBuf,
}

fn convert_option_as_error<T>(option: Option<T>) -> Result<T, Box<dyn Error>> {
    option.ok_or_else(|| NotABench::new_stripped_boxed())
}

impl BenchmarkId {
    fn convert_benchmark_result(&self, vec: Vec<BenchmarkResult>) -> DeviceBenchmark {
        DeviceBenchmark {
            device: self.device.clone(),
            benchmarks: vec,
        }
    }
}

#[derive(Ord, PartialOrd, Eq, PartialEq, Debug, Clone)]
pub struct DeviceBenchmark {
    pub benchmarks: Vec<BenchmarkResult>,
    pub device: String,
}

#[derive(PartialEq, Eq, Debug, Clone)]
pub enum Benchmarks {
    Sorting(String),
    Serialization(String),
    Signature(String),
    Combined(String),
}

impl From<Benchmarks> for String {
    fn from(dev: Benchmarks) -> Self {
        match dev {
            Benchmarks::Sorting(t) => t,
            Benchmarks::Serialization(t) => t,
            Benchmarks::Signature(t) => t,
            Benchmarks::Combined(t) => t
        }
    }
}

impl Benchmarks {
    fn new_sorting() -> Benchmarks {
        return Benchmarks::Sorting(String::from("Adding in sorted collections"));
    }

    fn new_serialization() -> Benchmarks {
        return Benchmarks::Serialization(String::from("Serialization of blocks"));
    }

    fn new_signature() -> Benchmarks {
        return Benchmarks::Signature(String::from("Creating signatures for transactions"));
    }

    fn new_combined() -> Benchmarks {
        return Benchmarks::Combined(String::from("placeholder"));
    }
}

#[derive(Default, Debug, Deserialize, Clone)]
#[serde(rename_all = "PascalCase")]
pub struct BenchmarkResult {
    pub benchmark: String,
    pub mode: String,
    pub threads: u32,
    pub samples: u32,
    pub score: f64,
    #[serde(rename = "Score Error (99.9%)")]
    pub error: f64,
    pub unit: String,
}

impl Ord for BenchmarkResult {
    fn cmp(&self, other: &Self) -> Ordering {
        self.score.partial_cmp(&other.score).unwrap_or(
            self.benchmark.cmp(&other.benchmark)
        )
    }
}

impl PartialOrd for BenchmarkResult {
    fn partial_cmp(&self, other: &Self) -> Option<Ordering> {
        Some(self.score.partial_cmp(&other.score).unwrap_or(
            self.benchmark.cmp(&other.benchmark)
        ))
    }
}

impl Eq for BenchmarkResult {}

impl PartialEq for BenchmarkResult {
    fn eq(&self, other: &Self) -> bool {
        self.benchmark.eq(&other.benchmark)
    }
}


#[derive(Default, Debug)]
pub struct BenchmarkCollector {
    pub benchmarks_by_device: Vec<(Benchmarks, DeviceBenchmarks)>
}

#[derive(Debug)]
pub struct DeviceBenchmarks {
    pub benchmark: String,
    pub date: NaiveDate,
    pub device_benchmarks: Vec<DeviceBenchmark>,
    pub original_path: PathBuf,
    pub unit: String,
}

impl Eq for DeviceBenchmarks {}

impl PartialEq for DeviceBenchmarks {
    fn eq(&self, other: &Self) -> bool {
        return self.benchmark.eq(&other.benchmark) && self.unit.eq(&other.unit);
    }
}

#[derive(Debug)]
struct NotABench {}

impl NotABench {
    fn new_stripped_boxed() -> Box<dyn Error> {
        Box::new(NotABench {})
    }
}

impl fmt::Display for NotABench {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        return write!(f, "Big oopsie, not a benchmark!");
    }
}

impl Error for NotABench {}

fn calculate_benchmark_type(prefix: &str) -> Result<Benchmarks, Box<dyn Error>> {
    return prefix.strip_prefix(SORTING_PREFIX).map_or_else(
        || prefix.strip_prefix(SERIALIZATION_PREFIX).map_or_else(
            || prefix.strip_prefix(SIGNATURE_PREFIX).map_or_else(
                || convert_option_as_error(prefix.strip_prefix(COMBINED_PREFIX).map(
                    |_| Benchmarks::new_combined()
                )),
                |_| Ok(Benchmarks::new_signature()),
            ), |_| Ok(Benchmarks::new_serialization()),
        ), |_| Ok(Benchmarks::new_sorting()),
    );
}

fn determine_benchmark_id(path: &PathBuf) -> Result<BenchmarkId, Box<dyn Error>> {
    let path_str = convert_option_as_error(path.file_name())?.to_string_lossy();
    let stripped_path_str = convert_option_as_error(path_str.strip_suffix(".csv"))?;
    let mut result_iter = stripped_path_str.split('_');
    let device = convert_option_as_error(result_iter.next())?.to_owned();
    let raw_bench = convert_option_as_error(result_iter.next())?.to_owned();
    let benchmark_type = calculate_benchmark_type(raw_bench.as_str())?;
    let raw_date = convert_option_as_error(result_iter.next())?;
    let date = NaiveDate::parse_from_str(raw_date, "%Y-%m-%d")?;
    let original_path = convert_option_as_error(path.parent())?.to_path_buf();
    return Ok(BenchmarkId {
        benchmark_type,
        date,
        device,
        original_path,
    });
}

fn strip_package(mut vec: Vec<BenchmarkResult>) -> Result<Vec<BenchmarkResult>, Box<dyn Error>> {
    for bench in vec.iter_mut() {
        bench.benchmark = convert_option_as_error(bench.benchmark
            .split('.').last())?.to_owned();
    }
    return Ok(vec);
}

pub fn build_from_dir(path: &str) -> Result<BenchmarkCollector, Box<dyn Error>> {
    let mut bench_col = BenchmarkCollector::default();
    let path = path.to_owned() + "**/*.csv";
    let glob = glob::glob(path.as_str())?;
    for path in glob {
        let path = path?;
        let benchmark_id = determine_benchmark_id(&path)?;
        let file = File::open(path)?;
        let mut benchs = strip_package(records_from_csv(file)?)?;
        benchs.sort_unstable();
        let first = convert_option_as_error(benchs.first())?.clone();
        let device_benchmarks = benchmark_id.convert_benchmark_result(benchs);
        let element = bench_col.benchmarks_by_device.iter_mut().find(
            |device| device.0 == benchmark_id.benchmark_type);
        match element {
            Some(t) => t.1.device_benchmarks.push(device_benchmarks),
            None => {
                let mut vec = Vec::new();
                vec.push(device_benchmarks);
                bench_col.benchmarks_by_device.push(
                    (benchmark_id.benchmark_type, DeviceBenchmarks {
                        benchmark: first.benchmark.clone(),
                        date: benchmark_id.date,
                        device_benchmarks: vec,
                        original_path: benchmark_id.original_path,
                        unit: first.unit.clone(),
                    })
                )
            }
        };
    }
    return Ok(bench_col);
}

fn records_from_csv(file: File) -> Result<Vec<BenchmarkResult>, Box<dyn Error>> {
    let reader = BufReader::new(file);
    let mut csv = csv::Reader::from_reader(reader);
    let mut vec = Vec::with_capacity(8);
    for result in csv.deserialize() {
        let record: BenchmarkResult = result?;
        vec.push(record);
    }
    return Ok(vec);
}