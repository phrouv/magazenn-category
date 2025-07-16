This directory contains a set of [Hyperfoil](https://hyperfoil.io) benchmarks. An instance of the [`rest-categories`](..) application [**MUST** be running somewhere](../README.md#running-the-application) prior to executing this benchmark.

Each benchmark can be customized via parameters. All the parameters are described in comments at the beginning of each benchmark file.

| Benchmark file                                     | Benchmark description                              |
|----------------------------------------------------|----------------------------------------------------|
| [`get-all-categories.hf.yml`](get-all-categories.hf.yml)   | Runs a `GET` to the `/api/categories` endpoint         |
| [`get-random-category.hf.yml`](get-random-category.hf.yml) | Runs a `GET`  to the `/api/categories/random` endpoint |
