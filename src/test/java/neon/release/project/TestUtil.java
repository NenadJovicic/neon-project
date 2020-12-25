package neon.release.project;

import neon.release.project.entity.Release;
import neon.release.project.entity.ReleaseStatus;
import neon.release.project.repository.ReleaseRepository;
import neon.release.project.repository.ReleaseStatusRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Stack;

public class TestUtil {
    public static final String[] possibleStatuses = new String[] {"Created", "In Development", "On DEV", "QA Done on DEV", "On staging", "QA done on STAGING", "On PROD", "Done"};

    @Autowired
    private ReleaseRepository releaseRepository;

    @Autowired
    private ReleaseStatusRepository releaseStatusRepository;

    public TestUtil() {}

    public TestUtil(ReleaseRepository releaseRepo, ReleaseStatusRepository releaseStatusRepo) {
        this.releaseRepository = releaseRepo;
        this.releaseStatusRepository = releaseStatusRepo;
    }

    public void loadTestData() {
        this.loadReleaseStatuses();
        this.loadReleases();
    }

    private void loadReleaseStatuses() {
        for(int i = 0; i < this.possibleStatuses.length; i++) {
            ReleaseStatus releaseStatus = ReleaseStatus.builder()
                    .id((long) (i + 1))
                    .statusName(this.possibleStatuses[i]).build();
            this.releaseStatusRepository.saveAndFlush(releaseStatus);
        }
    }

    private void loadReleases() {
        Release firstRelease = Release.builder()
                .id(1L)
                .name("First dev release")
                .releaseDate(new Date())
                .description("In development first release")
                .status(ReleaseStatus.builder().id(2L).build())
                .build();
        this.releaseRepository.saveAndFlush(firstRelease);
        Release secondRelease = Release.builder()
                .id(2L)
                .name("Release that is done")
                .releaseDate(new Date())
                .description("Description for release that has status done")
                .status(ReleaseStatus.builder().id((long) this.possibleStatuses.length).build())
                .build();
        this.releaseRepository.saveAndFlush(secondRelease);

    }
}
