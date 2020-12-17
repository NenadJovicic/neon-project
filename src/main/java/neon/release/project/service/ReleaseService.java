package neon.release.project.service;

import neon.release.project.entity.Release;
import neon.release.project.repository.ReleaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class ReleaseService {

    @Autowired
    private ReleaseRepository releaseRepository;

    public List<Release> getReleases() {
        return this.releaseRepository.findAll();
    }

    public Optional<Release> getReleaseById(Long id) {
        return this.releaseRepository.findById(id);
    }

    public Release createNewRelease(Release release) {
        return this.releaseRepository.saveAndFlush(release);
    }

    public Release updateRelease(Release release) {
        // if id is not provided, it will be actually creating new object instead of modifying existing one
        if (release.getId() != null) {
            // checking does release with provided id actually exist
            if (this.releaseRepository.existsById(release.getId())) {
                return this.releaseRepository.saveAndFlush(release);
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Release doesn't exist");
            }
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Release id required");
        }
    }

    public void deleteRelease(Long id) {
        if (!this.releaseRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Release with provided id does not exist");
        }
        this.releaseRepository.deleteById(id);
    }
}
