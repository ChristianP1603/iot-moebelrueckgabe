package jku.worker;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.response.ActivatedJob;
import io.camunda.client.annotation.JobWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Camunda Job Worker zum Versenden von Messages zwischen BPMN-Pools.
 *
 * Aufgaben:
 * - Jobs mit dem Typ "send-message" abholen
 * - Message-Name aus den BPMN-Headern lesen
 * - Correlation Key aus den Prozessvariablen ermitteln
 * - Camunda Message mit den vorhandenen Variablen veröffentlichen
 *
 * Verwendet:
 * - messageName aus dem BPMN-Header
 * - correlationKeyVariable aus dem BPMN-Header
 * - Prozessvariablen des aktuellen Jobs
 * 
 * Referenz
 *  https://docs.camunda.io/docs/apis-tools/java-client/job-worker/
 *  
 */

@Component
@Profile("camunda")
public class SendMessageWorker {

    // Logger für Konsolen-/Serverausgaben
    private static final Logger log = LoggerFactory.getLogger(SendMessageWorker.class);

    // Camunda-Client für Message-Versand
    private final CamundaClient camundaClient;

    // Konstruktor: Camunda-Client wird übergeben
    public SendMessageWorker(CamundaClient camundaClient) {
        this.camundaClient = camundaClient;

        // Log-Ausgabe beim Laden des Workers
        log.info("SendMessageWorker loaded.");
    }

    // Job Worker für alle BPMN-Elemente mit Job Type "send-message"
    @JobWorker(type = "send-message")
    public void sendMessage(ActivatedJob job) {

        // Header aus dem BPMN-Element lesen
        Map<String, String> headers = job.getCustomHeaders();

        // Prozessvariablen des aktuellen Jobs lesen
        Map<String, Object> variables = job.getVariablesAsMap();

        // Log-Ausgabe: Job wurde abgeholt
        log.info("SendMessageWorker triggered. elementId={}, headers={}, variables={}",
                job.getElementId(), headers, variables);

        // Name der zu sendenden Message aus Header lesen
        String messageName = headers.get("messageName");

        // Name der Variablen für den Correlation Key lesen
        // Fallback: "moebelId"
        String correlationKeyVariable = headers.getOrDefault("correlationKeyVariable", "moebelId");

        // Prüfen, ob Message-Name vorhanden ist
        if (messageName == null || messageName.isBlank()) {
            log.error("Header 'messageName' fehlt bei Element {}. Headers={}",
                    job.getElementId(), headers);
            throw new IllegalStateException(
                    "Header 'messageName' fehlt bei Element " + job.getElementId()
            );
        }

        // Correlation-Key-Wert aus den Variablen lesen
        Object correlationValue = variables.get(correlationKeyVariable);

        // Prüfen, ob der Correlation-Key vorhanden ist
        if (correlationValue == null) {
            log.error("Variable '{}' fehlt bei Element {}. Verfügbare Variablen: {}",
                    correlationKeyVariable, job.getElementId(), variables.keySet());
            throw new IllegalStateException(
                    "Variable '" + correlationKeyVariable + "' fehlt bei Element " + job.getElementId()
            );
        }

        // Log-Ausgabe: Message wird veröffentlicht
        log.info("Publishing message. messageName={}, correlationKeyVariable={}, correlationValue={}",
                messageName, correlationKeyVariable, correlationValue);

        // Camunda Message senden
        camundaClient.newPublishMessageCommand()
                .messageName(messageName)
                .correlationKey(String.valueOf(correlationValue))
                .variables(variables)
                .send()
                .join();

        // Log-Ausgabe: Message erfolgreich gesendet
        log.info("Message published successfully. elementId={}, messageName={}, correlationKey={}",
                job.getElementId(), messageName, correlationValue);
    }
}