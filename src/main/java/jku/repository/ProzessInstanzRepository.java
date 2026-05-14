package jku.repository;

import jku.entity.ProzessInstanz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProzessInstanzRepository extends JpaRepository<ProzessInstanz, Long> {
    List<ProzessInstanz> findByMoebelstuckId(Long moebelstuckId);
}
