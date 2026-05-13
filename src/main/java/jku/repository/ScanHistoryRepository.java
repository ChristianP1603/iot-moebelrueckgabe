package jku.repository;

import jku.entity.ScanHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScanHistoryRepository extends JpaRepository<ScanHistory, Long> {
    List<ScanHistory> findByMoebelstuckIdOrderByZeitstempelAsc(Long moebelstuckId);
    void deleteByMoebelstuckId(Long moebelstuckId);
}
