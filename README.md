# Prior Authorization Reference Implementation Provider Client Server

This is a Client Server for the Da Vinci Prior Authorization Reference Implementation, which can be found [here](https://github.com/HL7-DaVinci/prior-auth). The Client Server is used to be the provider using the Prior Auth service. Currently the server handles subscriptions (Rest-Hook and WebSocket).

## Requirements

- Java JDK 8

## Getting Started

Build, test, and start the microservice:

```
./gradlew install
./gradlew clean check
./gradlew run
```

Run in debug mode:

```
./gradlew run --args='debug'
```

Access the microservice:

```
curl http://localhost:9090/fhir/SubscriptionNotification
curl http://localhost:9090/fhir/Log
```

## Using Rest-Hook Subscriptions

The Client Server provides the `/SubscriptionNotification` endpoint which can be used to receive Rest-Hook notifications by the Prior Auth service. To use the Prior Auth service read the documentation on the [Prior Authorization Server Reference Implementation](https://github.com/HL7-DaVinci/prior-auth).

To use the Rest-Hook endpoint submit a Subscription to `/Subscription` on the Prior Auth server with the `channel.endpoint` set to `http://localhost:9090/fhir/SubscriptionNotification?identifier={identifier}&patient.identifier={patient}&status=active`.

When the ClaimResponse is updated the Prior Auth server will send a `GET` request to the `channel.endpoint` provided. The Client server decodes the request, polls for the updated ClaimResponse and then deletes the ClaimResponse if the `outcome` is `complete` or `error`.

## Using WebSocket Subscriptions

The `src/main/resources` directory provides the WebSocket subscription implementation. To use it open `index.html`. Once the Prior Auth server is running connect the WebSocket to the server by clicking the `Connect` button in the top left corner. When a Subscription logical id is ready to be bound enter the id in the text box and hit `Subscribe`.

## FHIR Services

The service endpoints in the table below are relative to `http://localhost:9090/fhir`.

| Service                     | Methods | Description                                                 |
| --------------------------- | ------- | ----------------------------------------------------------- |
| `/SubscriptionNotification` | `GET`   | Endpoint to send notifications for an update Claimresponse. |
| `/Log`                      | `GET`   | Gets the microservice log                                   |

## Questions and Contributions

Questions about the project can be asked in the [DaVinci stream on the FHIR Zulip Chat](https://chat.fhir.org/#narrow/stream/179283-DaVinci).

This project welcomes Pull Requests. Any issues identified with the RI should be submitted via the [GitHub issue tracker](https://github.com/HL7-DaVinci/prior-auth/issues).
