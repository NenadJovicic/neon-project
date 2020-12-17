package neon.release.project.controller;

import neon.release.project.entity.Release;
import neon.release.project.service.ReleaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * NOTE: Normally, Entity should not be return type of any of those routes, and I would introduce specific DTO objects
 * as return types, because returning entities as response can cause fetching data from linked tables that are maybe not needed
 */
@RestController
@RequestMapping(value = "/release")
public class ReleaseController {
    @Autowired
    private ReleaseService releaseService;

    @GetMapping()
    public List<Release> getReleases() {
        return this.releaseService.getReleases();
    }

    @PostMapping()
    public Release createRelease(@RequestBody Release release) {
        return this.releaseService.createNewRelease(release);
    }

    /**
     * Normally, PUT should contain "id" of entity that is modified, but if it is inside body, I don't see a reason to send it
     * @param release Release object that is modified
     * @return Updated release
     */
    @PutMapping()
    public Release updateRelease(@RequestBody Release release) {
        return this.releaseService.updateRelease(release);
    }

    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRelease(@PathVariable("id") Long id) {
        this.releaseService.deleteRelease(id);
    }
}
