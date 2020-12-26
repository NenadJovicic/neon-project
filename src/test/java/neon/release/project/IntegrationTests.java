package neon.release.project;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import neon.release.project.entity.Release;
import neon.release.project.entity.ReleaseStatus;
import neon.release.project.repository.ReleaseRepository;
import neon.release.project.repository.ReleaseStatusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.print.attribute.standard.Media;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = NeonReleaseApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class IntegrationTests {
    ObjectMapper mapper = new ObjectMapper();
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ReleaseRepository releaseRepository;

    @Autowired
    private ReleaseStatusRepository releaseStatusRepository;

    @BeforeEach
    void loadData() {
        TestUtil testUtil = new TestUtil(this.releaseRepository, this.releaseStatusRepository);
        testUtil.loadTestData();
    }

    @Test
    void fetchAllReleases() throws Exception {
        this.mvc.perform(get("/release").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("First dev release")))
                .andExpect(jsonPath("$[0].status.statusName", is(TestUtil.possibleStatuses[1])))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].status.statusName", is(TestUtil.possibleStatuses[TestUtil.possibleStatuses.length - 1])));
    }

    @Test
    void successfulGetReleaseById() throws Exception {
        this.mvc.perform(get("/release/1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status.id", is(2)));
    }

    @Test
    void getReleaseByIdWrongIdTypeProvidedError() throws Exception {
        this.mvc.perform(get("/release/s").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertEquals("Invalid release id", result.getResponse().getErrorMessage()));
    }

    @Test
    void getReleaseByIdNotFoundError() throws Exception {
        this.mvc.perform(get("/release/15").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals("Release does not exist", result.getResponse().getErrorMessage()));
    }

    @Test
    void createNewRelease() throws Exception {
        Release newRelease = Release.builder()
                .releaseDate(new Date())
                .description("New release that has status ON DEV")
                .status(ReleaseStatus.builder().id(3L).statusName(TestUtil.possibleStatuses[2]).build())
                .name("Dev release")
                .build();

        MvcResult result = this.mvc.perform(post("/release")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(newRelease)))
                .andExpect(status().isOk())
                // expecting 3 because it is autogenerated, and last id is 2
                .andExpect(jsonPath("$.id", is(3)))
                .andExpect(jsonPath("$.status.id", is(3)))
                .andExpect(jsonPath("$.status.statusName", is(TestUtil.possibleStatuses[2])))
                .andExpect(jsonPath("$.description", is(newRelease.getDescription())))
                .andExpect(jsonPath("$.name", is(newRelease.getName())))
                .andReturn();
        String bodyAsString = result.getResponse().getContentAsString();
        JsonNode jsonNode = mapper.readTree(bodyAsString);
        assertEquals(jsonNode.get("createdAt").toString(), (jsonNode.get("lastUpdateAt").toString()));
    }

    @Test
    void expectFailOnCreationIfProvidedReleaseStatusDoesNotExist() throws Exception {
        Release newRelease = Release.builder()
                .name("fail release")
                .description("random description")
                .status(ReleaseStatus.builder().id(25L).statusName("Random").build())
                .releaseDate(new Date())
                .build();
        this.mvc.perform(post("/release")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(newRelease)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals("Release status does not exist", result.getResponse().getErrorMessage()));
    }

    @Test
    void updateExistingReleaseAndCheckIsLastUpdateAtChanged() throws Exception {
        String newName = "New edited name of release";
        MvcResult getReleaseResult = mvc.perform(get("/release/1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andReturn();
        Release releaseForUpdate = mapper.readValue(getReleaseResult.getResponse().getContentAsString(), Release.class);
        releaseForUpdate.setName(newName);
        MvcResult updatedReleaseResult = mvc.perform(put("/release")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(releaseForUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(newName)))
                .andReturn();
        Release updatedRelease = mapper.readValue(updatedReleaseResult.getResponse().getContentAsString(), Release.class);
        assertTrue(updatedRelease.getLastUpdateAt().getTime() > releaseForUpdate.getLastUpdateAt().getTime());
    }

    @Test
    void updateReleaseNonExistingIdProvidedError() throws Exception {
        Release release = Release.builder()
                .id(15L)
                .name("name")
                .description("description")
                .status(ReleaseStatus.builder().id(1L).statusName(TestUtil.possibleStatuses[0]).build())
                .releaseDate(new Date())
                .build();
        mvc.perform(put("/release").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(release)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals("Release does not exist", result.getResponse().getErrorMessage()));
    }

    @Test
    void updateReleaseNoIdProvidedError() throws Exception {
        Release release = Release.builder()
                .name("name")
                .description("description")
                .status(ReleaseStatus.builder().id(1L).statusName(TestUtil.possibleStatuses[0]).build())
                .releaseDate(new Date())
                .build();
        mvc.perform(put("/release").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(release)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertEquals("Release id required", result.getResponse().getErrorMessage()));
    }

    @Test
    void deleteRelease() throws Exception {
        mvc.perform(delete("/release/1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        mvc.perform(get("/release/1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteReleaseNotFoundError() throws Exception {
        mvc.perform(delete("/release/15"))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals("Release does not exist", result.getResponse().getErrorMessage()));
    }

}
