package jku;

import io.camunda.client.CamundaClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class BpmnDeployer implements ApplicationRunner {

    @Autowired(required = false)
    private CamundaClient camundaClient;

    @Override
    public void run(ApplicationArguments args) {
        if (camundaClient == null) {
            System.out.println("Camunda nicht verbunden, BPMN-Deployment uebersprungen.");
            return;
        }
        try {
            camundaClient.newDeployResourceCommand()
                    .addResourceFromClasspath("moebelprozess.bpmn")
                    .send()
                    .join();
            System.out.println("BPMN deployed.");
        } catch (Exception e) {
            System.out.println("BPMN deployment fehlgeschlagen: " + e.getMessage());
        }
    }
}
