package jku.worker;

import jku.entity.Moebelstuck;
import jku.repository.MoebelstuckRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class AusSystemEntfernenWorkerTest {

    @Test
    void entferneSetztEntfernt() {
        MoebelstuckRepository repo = mock(MoebelstuckRepository.class);
        Moebelstuck m = new Moebelstuck();
        when(repo.findById(1L)).thenReturn(Optional.of(m));

        new AusSystemEntfernenWorker(repo).entferne(1L);

        assertTrue(m.isEntfernt());
        verify(repo).save(m);
    }
}
