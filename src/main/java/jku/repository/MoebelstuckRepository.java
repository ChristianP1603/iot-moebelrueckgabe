package jku.repository;

import jku.entity.Moebelstuck;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MoebelstuckRepository extends JpaRepository<Moebelstuck, Long> {
    Optional<Moebelstuck> findByNfcTagId(String nfcTagId);
}
