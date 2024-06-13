* ~~I created this project so I can populate weather related data to nuxt-quasar-playground project.
  nuxt-quasar-playground resides in OneDrive.~~
* ~~Influx token to create users and what not is
  ZiBCrqN79L1rhpNlTwcIw9hIx3IN8Rh8xAzjM5xYdXAYdFccMSv51W7b1G2e-UTzQwT1512Gkhf7egbDFrV0jA==~~

## What

* This project was initially created to learn about Nuxt and what not. But it quickly turned in to Oculus's ETL
  component.

### Influx

* device
    * tags: id, acna, sponsored_by
    * fields: avg_cpu_load, max_cpu_load, avg_memory_used, avg_memory_used

## `compose.yaml` contains a workable example.

* It spins up a cluster with multiple Kafka brokers and a Kafbat UI to manage those.

## `flink_compose.yaml`

* Plan is to start Flink in Application mode. I will use this yaml to do that.
*

See [this](https://nightlies.apache.org/flink/flink-docs-master/docs/deployment/resource-providers/standalone/docker/#application-mode-1)

# For Insync

node_modules
.git
.idea
yarn.lock
*.iml
.nuxt
target

## IMPORTANT LINKS

1. If you ever want to know `listeners` read [this](https://rmoff.net/2018/08/02/kafka-listeners-explained/)