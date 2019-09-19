# Prior Authorization Reference Implementation Provider Client Server

This is a Client Server for the Da Vinci Prior Authorization Reference Implementation, which can be found [here](https://github.com/HL7-DaVinci/prior-auth).

## Requirements

- Java JDK 8

## Getting Started

Build, test, and start the microservice:

```
./gradlew install
./gradlew clean check
./gradlew run
```

Access the microservice:

```
curl http://localhost:9090/fhir/SubscriptionNotification
curl http://localhost:9090/fhir/Log
```

## Usage

First run the service:

```
./gradlew run
```

Run the Prior Authorization Server [(Reference Implementation)](https://github.com/HL7-DaVinci/prior-auth) and submit a PriorAuth request to `/Claim/$submit`. When a ClaimResponse comes back as `queued` or `PENDING` add a RESTHOOK subscription by `POST`ing the following to `/fhir/Subscription` to the PriorAuth Server:

```
{
  "resourceType": "Subscription",
  "criteria": "identifier=[priorauth identifier]&patient.identifier=[patient id]&status=active",
  "channel": {
    "type": "rest-hook",
    "endpoint": "http://localhost:9090/fhir/SubscriptionNotification?identifier=[priorauth identifier]&patient.identifier=[patient id]&status=active"
  }
}
```

This will create a new subscription. When a new ClaimResponse is created the notification will be sent to `fhir/SubscriptionNotification`.

## FHIR Services

The service endpoints in the table below are relative to `http://localhost:9090/fhir`.

| Service                     | Methods | Description                                                 |
| --------------------------- | ------- | ----------------------------------------------------------- |
| `/SubscriptionNotification` | `GET`   | Endpoint to send notifications for an update Claimresponse. |
| `/Log`                      | `GET`   | Gets the microservice log                                   |

## Questions and Contributions

Questions about the project can be asked in the [DaVinci stream on the FHIR Zulip Chat](https://chat.fhir.org/#narrow/stream/179283-DaVinci).

This project welcomes Pull Requests. Any issues identified with the RI should be submitted via the [GitHub issue tracker](https://github.com/HL7-DaVinci/prior-auth/issues).
