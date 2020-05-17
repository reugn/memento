# memento
[![Build Status](https://travis-ci.com/reugn/memento.svg?branch=master)](https://travis-ci.com/reugn/memento)

With [Kafka](https://kafka.apache.org/), we can't keep on retry a message without blocking the whole partition. But what if we would like
to delay/reprocess the current message and go ahead.
Kafka lacks a delayed message consumption mechanism as well.
What if we could hack the Kafka Streams and turn the flow to a delayed Kafka producer...

## Introduction
`memento` is a Kafka Streams application that could come in handy when you want to:
* reprocess particular Kafka messages without blocking the partition
* submit a delayed Kafka message

The message should contain the following headers:
* `origin` - a target topic name
* `ts` - a timestamp to emit the message

Delayed message submission via HTTP is also available.
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

The project utilizes the `KeyValueStore` as a messages storage. Inject your own implementation using `Guice` module.  
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
