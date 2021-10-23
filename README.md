# memento
[![Build](https://github.com/reugn/memento/actions/workflows/build.yml/badge.svg)](https://github.com/reugn/memento/actions/workflows/build.yml)

Using [Apache Kafka](https://kafka.apache.org/), we can't keep on retrying a message without blocking the entire partition. But what if we would like
to delay/reprocess the current message and move on.
Kafka does not have a delayed message reception mechanism out of the box.
What if we could hack Kafka Streams and turn the flow into a delayed Kafka producer...

## Introduction
`memento` is a Kafka Streams application that could come in handy when you need to:
* Reprocess particular Kafka messages without blocking the partition
* Submit a delayed Kafka message

The message should contain the following headers:
* `origin` - a target topic name
* `ts` - a timestamp to emit the message

Delayed message submission over HTTP is also supported.
```
curl --location --request POST 'localhost:8080/store' \
--header 'Content-Type: application/json' \
--data-raw '{
"key": "key1",
"value": "abcd",
"origin": "test2",
"ts": 1588338000000
}'
```

The project utilizes the `KeyValueStore` as message storage. Inject your own implementation using the `Guice` module.  
More persistent KeyValueStores:
* [kafka-aerospike-state-store](https://github.com/reugn/kafka-aerospike-state-store)

## Getting started
`memento` is a Scala sbt application.  
Run locally:
```
sbt run -Dconfig.resource=application.conf
```
Build an application jar:
```
sbt assembly
```
Run from the jar:
```
java -jar assembly/memento-<version>.jar -Dconfig.resource=conf/application.conf
```

## License
Licensed under the Apache 2.0 License.
