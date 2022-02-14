package org.hl7.davinci.providerclient;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.hl7.davinci.providerclient.PALogger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;


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
  private static final String BASE_URL = "http://localhost:9015/fhir/";
  private static final String CLAIM_RESPONSE = "ClaimResponse";
  private static final String SUBSCRIPTION = "Subscription";

  @GetMapping("")
  public ResponseEntity<String> subscriptionNotification(@RequestParam(name = "identifier") String id,
  @RequestParam(name = "patient.identifier") String patient, @RequestParam(name = "status") String status) {
    logger.info("SubscriptionNotificationEndpoint::Notification(" + id + ", " + patient + ", " + status + ")");
    HttpStatus returnStatus = HttpStatus.OK;

    try {
      // Send REST request for the new ClaimResponse...
      String url = BASE_URL + CLAIM_RESPONSE + "?identifier=" + id + "&patient.identifier=" + patient + "&status="
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
      if (outcome == RemittanceOutcome.COMPLETE || outcome == RemittanceOutcome.ERROR) {
        // Delete the subscription...
        logger.info("SubscriptionNotificationEndpoint::Delete Subscription (" + id + ", " + patient + ")");
        url = BASE_URL + SUBSCRIPTION + "?identifier=" + id + "&patient.identifier=" + patient;
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
