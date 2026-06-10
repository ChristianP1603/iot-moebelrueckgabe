package jku.worker;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.response.ActivatedJob;
import io.camunda.client.annotation.JobWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConditionalOnBean(CamundaClient.class)
public class SendMessageWorker {

    private static final Logger log = LoggerFactory.getLogger(SendMessageWorker.class);

    private final CamundaClient camundaClient;

    public SendMessageWorker(CamundaClient camundaClient) {
        this.camundaClient = camundaClient;
        log.info("SendMessageWorker loaded.");
    }

    @JobWorker(type = "send-message")
    public void sendMessage(ActivatedJob job) {
        Map<String, String> headers = job.getCustomHeaders();
        Map<String, Object> variables = job.getVariablesAsMap();

        log.info("SendMessageWorker triggered. elementId={}, headers={}, variables={}",
                job.getElementId(), headers, variables);

        String messageName = headers.get("messageName");
        String correlationKeyVariable = headers.getOrDefault("correlationKeyVariable", "moebelId");

        if (messageName == null || messageName.isBlank()) {
            log.error("Header 'messageName' fehlt bei Element {}. Headers={}",
                    job.getElementId(), headers);
            throw new IllegalStateException(
                    "Header 'messageName' fehlt bei Element " + job.getElementId()
            );
        }

        Object correlationValue = variables.get(correlationKeyVariable);

        if (correlationValue == null) {
            log.error("Variable '{}' fehlt bei Element {}. Verfügbare Variablen: {}",
                    correlationKeyVariable, job.getElementId(), variables.keySet());
            throw new IllegalStateException(
                    "Variable '" + correlationKeyVariable + "' fehlt bei Element " + job.getElementId()
            );
        }

        log.info("Publishing message. messageName={}, correlationKeyVariable={}, correlationValue={}",
                messageName, correlationKeyVariable, correlationValue);

        camundaClient.newPublishMessageCommand()
                .messageName(messageName)
                .correlationKey(String.valueOf(correlationValue))
                .variables(variables)
                .send()
                .join();

        log.info("Message published successfully. elementId={}, messageName={}, correlationKey={}",
                job.getElementId(), messageName, correlationValue);
    }
}