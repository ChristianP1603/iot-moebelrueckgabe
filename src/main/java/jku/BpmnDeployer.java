package jku;

import io.camunda.client.CamundaClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class BpmnDeployer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(BpmnDeployer.class);

    @Autowired(required = false)
    private CamundaClient camundaClient;

    @Override
    public void run(ApplicationArguments args) {
        if (camundaClient == null) {
            log.info("Camunda nicht verbunden, BPMN-Deployment uebersprungen.");
            return;
        }
        try {
            camundaClient.newDeployResourceCommand()
                    .addResourceFromClasspath("moebelprozessNEU.bpmn")
                    .send()
                    .join();
            log.info("BPMN deployed.");
        } catch (Exception e) {
            log.warn("BPMN deployment fehlgeschlagen: {}", e.getMessage());
        }
    }
}
