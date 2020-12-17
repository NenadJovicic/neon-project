package neon.release.project.repository;

import neon.release.project.entity.ReleaseStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReleaseStatusRepository extends JpaRepository<ReleaseStatus, Long> {
}
