package jku.worker;

import io.camunda.client.annotation.JobWorker;
import io.camunda.client.api.response.ActivatedJob;
import jku.repository.MoebelstuckRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("camunda")
public class VerfuegbarWorker {

    private final MoebelstuckRepository moebelRepo;

    public VerfuegbarWorker(MoebelstuckRepository moebelRepo) {
        this.moebelRepo = moebelRepo;
    }

    @JobWorker(type = "moebel-verfuegbar-setzen")
    public void setVerfuegbar(ActivatedJob job) {
        Object moebelId = job.getVariablesAsMap().get("moebelId");
        if (moebelId != null) {
            markiereVerfuegbar(Long.valueOf(moebelId.toString()));
        }
    }

    void markiereVerfuegbar(Long moebelId) {
        moebelRepo.findById(moebelId).ifPresent(m -> {
            m.setVerfuegbar(true);
            moebelRepo.save(m);
        });
    }
}
