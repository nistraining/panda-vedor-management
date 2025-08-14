#!/bin/bash

# Give Docker DNS a moment to settle
sleep 5

echo "Waiting for Kafka broker at kafka-broker-0.kafka-broker.default.svc.cluster.local:9092..."
until nc -z kafka-broker-0.kafka-broker.default.svc.cluster.local 9092; do
  echo "Kafka broker not yet available..."
  sleep 2
done
echo "Kafka broker is up!"

echo "Waiting for Schema Registry at schema-registry-service:8081..."
until nc -z schema-registry-service 8081; do
  echo "Schema Registry not yet available..."
  sleep 2
done
echo "Schema Registry is up!"

echo "Starting Vendor Service..."
exec java -jar panda-vendor-management.war