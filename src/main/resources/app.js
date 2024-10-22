// Define BASE_URL here
const BASE_URL = "http://localhost:9015"; // Replace with the correct base URL for your server

var stompClient = null;

function setConnected(connected) {
  $("#connect").prop("disabled", connected);
  $("#disconnect").prop("disabled", !connected);
  if (connected) {
    $("#conversation").show();
  } else {
    $("#conversation").hide();
  }
  $("#messages").html("");
}

function connect() {
  socket = new WebSocket("ws://localhost:9015/fhir/connect");
  stompClient = Stomp.over(socket);
  stompClient.connect({}, function(frame) {
    setConnected(true);
    console.log("Connected: " + frame);
    stompClient.subscribe("/private/notification", function(msg) {
      showMessage(msg.body);
    }, function(error) {
      console.log("Connection failed: " + error);
      alert("Failed to connect. Please try again.");
    });
  });
}

function disconnect() {
  if (stompClient !== null) {
    stompClient.disconnect();
  }
  setConnected(false);
  console.log("Disconnected");
}

function bindId() {
  stompClient.send("/subscribe", {}, "bind: " + $("#subscriptionId").val());
}

function showMessage(message) {
  $("#messages").append("<tr><td>" + message + "</td></tr>");
}

function createRestHookSubscription() {
  const claimResponseIdentifier = $("#claimResponseIdentifier").val(); // Get identifier from input
  const patientIdentifier = $("#patientIdentifier").val(); // Get patient identifier from input

  const subscriptionData = {
    resourceType: "Subscription",
    meta: {
      profile: [
        "http://hl7.org/fhir/uv/subscriptions-backport/StructureDefinition/backport-subscription"
      ]
    },
    text: {
      status: "generated",
      div: "<div xmlns=\"http://www.w3.org/1999/xhtml\"><p><b>Generated Narrative: Subscription</b><a name=\"subscription-admission\"></a></p></div>"
    },
    status: "active",
    end: "2023-12-31T12:00:00Z", // Subscription end date
    reason: "Topic-Based Subscription for PAS",
    criteria: "http://hl7.org/SubscriptionTopic/priorauth", // Topic-based subscription criteria
    _criteria: {
      extension: [
        {
          url: "http://hl7.org/fhir/uv/subscriptions-backport/StructureDefinition/backport-filter-criteria",
          valueString: `ClaimResponse?identifier=${claimResponseIdentifier}&patient.identifier=${patientIdentifier}&status=active`        }
      ]
    },
    channel: {
      extension: [
        {
          url: "http://hl7.org/fhir/uv/subscriptions-backport/StructureDefinition/backport-heartbeat-period",
          valueUnsignedInt: 86400 // Heartbeat every 24 hours (in seconds)
        },
        {
          url: "http://hl7.org/fhir/uv/subscriptions-backport/StructureDefinition/backport-timeout",
          valueUnsignedInt: 60 // Timeout in seconds
        },
        {
          url: "http://hl7.org/fhir/uv/subscriptions-backport/StructureDefinition/backport-max-count",
          valuePositiveInt: 20 // Max number of notifications
        }
      ],
      type: "rest-hook",
      endpoint: `http://localhost:9015/notify/${claimResponseIdentifier}`, // Use dynamic endpoint for notifications      payload: "application/fhir+json",
      _payload: {
        extension: [
          {
            url: "http://hl7.org/fhir/uv/subscriptions-backport/StructureDefinition/backport-payload-content",
            valueCode: "id-only" // Only send resource IDs in the payload
          }
        ]
      }
    }
  };

  $.ajax({
    url: BASE_URL + "/fhir/Subscription",
    type: "POST",
    data: JSON.stringify(subscriptionData),
    contentType: "application/fhir+json",
    success: function(response) {
      alert("Subscription created successfully");
      console.log(response);
    },
    error: function(error) {
      alert("Failed to create subscription");
      console.log(error);
      console.log("Response text:", error.responseText);
    }
  });
}

$(function() {
  $("form").on("submit", function(e) {
    e.preventDefault();
  });
  $("#connect").click(function() {
    connect();
  });
  $("#disconnect").click(function() {
    disconnect();
  });
  $("#send").click(function() {
    bindId();
  });
  $("#create-resthook").click(function() {
    createRestHookSubscription();
  });
});
