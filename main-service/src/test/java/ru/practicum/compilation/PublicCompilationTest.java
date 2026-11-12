package ru.practicum.compilation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Интеграционные тесты Пабилк Подборки")
@Transactional
public class PublicCompilationTest extends BaseTestClass {
    public static final String URL = "/compilations";
    private final Set<Long> setEvents = new HashSet<>();
    private long compId = 1;

    @BeforeEach
    void setUp() throws Exception {
        long idUser = getIdFromObject(createUser());
        long catId = getIdFromObject(createCategory());
        long id = getIdFromObject(createEvent(idUser, catId));
        setEvents.add(id);
        compId = getIdFromObject(createCompilation(setEvents, false, "New compilation" + compId));
    }


    @Test
    public void getCompilations_ShouldReturnList() throws Exception {
        compId = getIdFromObject(createCompilation(setEvents, true, "New compilation " + compId));
        mockMvc.perform(get(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("pinned", "true")
                        .param("size", "10")
                        .param("from", "0")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1L))
                .andExpect(status().isOk());
    }

    // default params  pinned = false, size = 10, from = 0
    @Test
    public void getCompilationsWithOutParam_ShouldReturnList() throws Exception {
        mockMvc.perform(get(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").exists())
                .andExpect(status().isOk());
    }

    @Test
    public void getCompilationsWithWrongParams_ShouldReturnBadRequestWithApiError() throws Exception {
        mockMvc.perform(get(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("pinned", "wrong")
                        .param("size", "wrong")
                        .param("from", "wrong")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

    }

    @Test
    public void getCompilationById_ShouldReturnCompilation() throws Exception {
        mockMvc.perform(get("/compilations/" + compId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").exists())
                .andExpect(jsonPath("$.id").value(compId));

    }

    @Test
    public void getCompilationByWrongId_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/compilations/" + (compId + 1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

    }
}
