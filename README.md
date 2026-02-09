# Specmatic Kafka Sample

* [Specmatic Website](https://specmatic.io)
* [Specmatic Documentation](https://specmatic.io/documentation.html)

This sample project demonstrates how we can run contract tests against a service which interacts with a kafka broker.

Here we are using [AsyncAPI 3.0.0 specification](https://www.asyncapi.com/docs/reference/specification/v3.0.0). For equivalent [AsyncAPI 2.6.0 API specification](https://www.asyncapi.com/blog/release-notes-2.6.0) based implementation, please refer to [this repository](https://github.com/znsio/specmatic-kafka-sample).

## Background

This project includes a consumer that listens to messages on receive topic for requests and then upon receiving a message, it processes the same and publishes a reply message to send topic.
Thereby it demonstrates the [request reply pattern](https://www.asyncapi.com/docs/tutorials/getting-started/request-reply) in AsyncAPI 3.0.0 specification also.

![Specmatic Kafka Sample Architecture](AsyncAPI-Request-Reply-Draft.gif)

## Pre-requisites
* Gradle
* JDK 17+

## Run the tests

### 1. Using Specmatic-JUnit Helper

```shell
./gradlew test --tests="com.example.order.ContractTest"
```

### 2. Using TestContainers

```shell
./gradlew test --tests="com.example.order.ContractTestUsingTestContainer"
```

You will now see a detailed HTML report in `build/reports/index.html` with the messages that were sent and received as part of the contract tests.

## Run the contract tests using specmatic-kafka docker image

1. Start the kafka broker using below command.
   ```shell
   docker compose up -d
   ```
2. Create the required topics in the running Kafka broker.
   # Copy the topic creation script into the Kafka container and execute it:
    ```shell
   docker cp create-topics.sh kafka:/tmp/create-topics.sh
   docker exec kafka bash /tmp/create-topics.sh
   ```
   
3. Run the application.
   ```shell
   ./gradlew bootRun
   ```
   
4. Run the Order Status Service as a mock server using the specmatic enterprise docker image.
   ```shell
   docker run --rm --network host -v "$(pwd):/usr/src/app" specmatic/enterprise mock
   ```
   
5. Run the contract tests.
   - On Unix and Windows Powershell:
   ```shell
   docker run --rm --network host -v "$(pwd):/usr/src/app" specmatic/enterprise test
   ```

6. Bring down the Kafka broker after the tests are done.
   ```shell
   docker compose down
   ```