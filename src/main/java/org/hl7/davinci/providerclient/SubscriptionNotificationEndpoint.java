package org.hl7.davinci.providerclient;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.uhn.fhir.parser.JsonParser;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.ClaimResponse;
import org.hl7.fhir.r4.model.ClaimResponse.RemittanceOutcome;

import ca.uhn.fhir.parser.IParser;
import okhttp3.OkHttpClient;

/**
 * The SubscriptionNotification endpoint to send subscriptions notifications to
 * and process them
 */
@CrossOrigin
@RestController
@RequestMapping("SubscriptionNotification")
public class SubscriptionNotificationEndpoint {

  static final Logger logger = PALogger.getLogger();
  private static final String BASE_URL = "http://localhost:9015";
  private static final String CLAIM_RESPONSE = "ClaimResponse";
  private static final String SUBSCRIPTION = "Subscription";

  @Autowired
  private Environment env;

  @PostMapping(value = "", consumes = { MediaType.APPLICATION_JSON_VALUE, "application/fhir+json" })
  @ResponseBody
  public ResponseEntity<String> subscriptionNotification(HttpEntity<String> entity, @RequestParam(name = "identifier") String id,
                                                         @RequestParam(name = "patient.identifier") String patient, @RequestParam(name = "status") String status) {

    logger.info("SubscriptionNotificationEndpoint::Notification(" + id + ", " + patient + ", " + status + ")");
    logger.info("Notification Body: " + entity.getBody());
    HttpStatus returnStatus = HttpStatus.OK;

    try {
      String baseURL = env.getProperty("prior_auth_server_url");

      if (baseURL == null || baseURL.isEmpty()) {
        baseURL = BASE_URL;
      }

      // Send REST request for the new ClaimResponse...
      String url = baseURL + "/fhir/" + CLAIM_RESPONSE + "?identifier=" + id + "&patient.identifier=" + patient + "&status="
          + status;
      logger.fine("SubscriptionNotificationEndpoint::url(" + url + ")");
      OkHttpClient client = new OkHttpClient();
      okhttp3.Response response = client
          .newCall(new okhttp3.Request.Builder().header("Accept", "application/fhir+json").url(url).build()).execute();
      IParser parser = App.FHIR_CTX.newJsonParser();
      Bundle claimResponseBundle = (Bundle) parser.parseResource(response.body().string());
      logger.info("SubscriptionNotificationEndpoint::Received Bundle " + claimResponseBundle.getId());

      // Check the ClaimResponse outcome...
      ClaimResponse claimResponse = (ClaimResponse) claimResponseBundle.getEntry().get(0).getResource();
      RemittanceOutcome outcome = claimResponse.getOutcome();

      logger.info("Claim Response Outcome:" + outcome);
      if (outcome == RemittanceOutcome.COMPLETE || outcome == RemittanceOutcome.ERROR) {
        //Get SubscriptionId from the response body
        Bundle notification = parser.parseResource(Bundle.class, entity.getBody());
        Resource subscriptionParams = notification.getEntry().get(0).getResource();
        String subscriptionId = ((Parameters) subscriptionParams).getParameter().get(0).getResource().getIdElement().getIdPart();

        // Delete the subscription...
        logger.info("SubscriptionNotificationEndpoint::Delete Subscription (" + subscriptionId + ", " + patient + ")");
        url = baseURL + "/fhir/" + SUBSCRIPTION + "?identifier=" + subscriptionId + "&patient.identifier=" + patient;
        logger.fine("SubscriptionNotificationEndpoint::url(" + url + ")");
        response = client
            .newCall(new okhttp3.Request.Builder().header("Accept", "application/fhir+json").url(url).delete().build())
            .execute();
      }
    } catch (IOException e) {
      returnStatus = HttpStatus.BAD_REQUEST;
      logger.log(Level.SEVERE, "SubscriptionNotificationEndpoint::IOException in polling request", e);
    }
    return new ResponseEntity<>(returnStatus);
  }
}
