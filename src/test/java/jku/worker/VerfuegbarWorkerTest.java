package jku.worker;

import jku.entity.Moebelstuck;
import jku.repository.MoebelstuckRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class VerfuegbarWorkerTest {

    @Test
    void markiereVerfuegbarSetztTrue() {
        MoebelstuckRepository repo = mock(MoebelstuckRepository.class);
        Moebelstuck m = new Moebelstuck();
        when(repo.findById(1L)).thenReturn(Optional.of(m));

        new VerfuegbarWorker(repo).markiereVerfuegbar(1L);

        assertTrue(m.getVerfuegbar());
        verify(repo).save(m);
    }
}
