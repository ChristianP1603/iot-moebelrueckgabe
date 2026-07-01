package jku.worker;

import io.camunda.client.annotation.JobWorker;
import io.camunda.client.api.response.ActivatedJob;
import jku.repository.MoebelstuckRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("camunda")
public class AusSystemEntfernenWorker {

    private final MoebelstuckRepository moebelRepo;

    public AusSystemEntfernenWorker(MoebelstuckRepository moebelRepo) {
        this.moebelRepo = moebelRepo;
    }

    @JobWorker(type = "moebel-aus-system-entfernen")
    public void ausSystemEntfernen(ActivatedJob job) {
        Object moebelId = job.getVariablesAsMap().get("moebelId");
        if (moebelId != null) {
            entferne(Long.valueOf(moebelId.toString()));
        }
    }

    void entferne(Long moebelId) {
        moebelRepo.findById(moebelId).ifPresent(m -> {
            m.setEntfernt(true);
            moebelRepo.save(m);
        });
    }
}
