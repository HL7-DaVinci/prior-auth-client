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
  socket = new WebSocket("ws://localhost:9000/fhir/connect");
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
  const subscriptionData = {
    resourceType: "Subscription",
    // Add other necessary FHIR Subscription fields here
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
