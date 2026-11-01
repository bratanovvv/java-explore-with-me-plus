package ru.practicum.compilation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.compilation.dto.RequestCompilationDto;

import java.util.HashSet;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Интеграционные тесты Админ: Подборки")
@Transactional
public class AdminCompilationTest extends CompilationBaseTestClass {
    private static final String URL = "/admin/compilations";
    private Set<Long> setEvents = new HashSet<>();
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws Exception {
        long idUser = getIdFromObject(createUser());
        long catId = getIdFromObject(createCategory());
        long idEvent = getIdFromObject(createEvent(idUser, catId));
        setEvents.add(idEvent);
    }

    @Test
    public void createCompilation_ShouldReturnNewCompilationWithID() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(RequestCompilationDto.builder()
                                .events(setEvents)
                                .pinned(false)
                                .title("New compilation Created")
                                .build())))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("New compilation Created"))
                .andExpect(status().isCreated());
    }

    @Test
    public void createCompilationWithNotExistEvent_ShouldReturnNotFound() throws Exception {
        setEvents.add(999L);

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(RequestCompilationDto.builder()
                                .events(setEvents)
                                .pinned(false)
                                .title("New compilation Created")
                                .build())))
                .andExpect(status().isNotFound());
    }

    @Test
    public void createCompilationWithWrongField_ShouldReturnApiError() throws Exception {
        String json = objectMapper.writeValueAsString(RequestCompilationDto.builder()
                .events(setEvents)
                .pinned(false)
                .title("New compilation Created")
                .build());

        json = json.replace("false", "truberg");

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void deleteCompilation_shouldReturnOK() throws Exception {
        var idCom = getIdFromObject(createCompilation(setEvents, false, "Title"));

        mockMvc.perform(delete(URL + "/" + idCom).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    public void deleteCompilationWithWrongId_shouldReturnNotFound() throws Exception {
        var idCom = getIdFromObject(createCompilation(setEvents, false, "Title")) + 1;

        mockMvc.perform(delete(URL + "/" + idCom).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void updateCompilationTitle_ShouldReturnOkAndNewCompilation() throws Exception {
        var idCom = getIdFromObject(createCompilation(setEvents, false, "Title"));
        String json = """
                {"title" : "UpdateTitle"}
                """;
        mockMvc.perform(patch(URL + "/" + idCom).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("UpdateTitle"));
    }

    @Test
    public void updateCompilationEvent_ShouldReturnOkAndNewCompilation() throws Exception {
        var idCom = getIdFromObject(createCompilation(setEvents, false, "Title"));
        String json = """
                {"events" : []}
                """;
        mockMvc.perform(patch(URL + "/" + idCom).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.events.length()").value(0));
    }

    @Test
    public void updateCompilationPinned_ShouldReturnOkAndNewCompilation() throws Exception {
        var idCom = getIdFromObject(createCompilation(setEvents, false, "Title"));
        String json = """
                {"pinned" : true}
                """;
        mockMvc.perform(patch(URL + "/" + idCom).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pinned").value("true"));
    }

    @Test
    public void updateCompilationNotExisting_ShouldReturnNotFound() throws Exception {
        var idCom = getIdFromObject(createCompilation(setEvents, false, "Title")) + 1;
        String json = """
                {"title" : "UpdateTitle"}
                """;
        mockMvc.perform(patch(URL + "/" + idCom).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isNotFound());
    }
}
