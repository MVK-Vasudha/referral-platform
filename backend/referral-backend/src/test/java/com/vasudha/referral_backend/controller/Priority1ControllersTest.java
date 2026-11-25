package com.vasudha.referral_backend.controller;

import com.vasudha.referral_backend.dto.ConnectionDTO;
import com.vasudha.referral_backend.entity.Role;
import com.vasudha.referral_backend.entity.User;
import com.vasudha.referral_backend.exception.GlobalExceptionHandler;
import com.vasudha.referral_backend.repository.UserRepository;
import com.vasudha.referral_backend.service.ConnectionService;
import com.vasudha.referral_backend.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class Priority1ControllersTest {

    @Mock
    private UserService userService;

    @Mock
    private ConnectionService connectionService;

    @Mock
    private UserRepository userRepository;

    @Test
    void searchUsers_rejectsInvalidPaginationWithBadRequest() throws Exception {
        UserController controller = new UserController(userService, userRepository);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockMvc.perform(get("/users/search")
                        .param("page", "-1")
                        .param("size", "10")
                        .principal(new UsernamePasswordAuthenticationToken("alice", null)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void mutualConnectionsAndDelete_forwardCurrentUserIdFromAuthentication() throws Exception {
        User alice = User.builder().id(1L).username("alice").role(Role.USER).build();
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));
        when(connectionService.getMutualConnections(eq(1L), eq(2L), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(
                        new ConnectionDTO(null, 3L, "charlie", "Charlie", "SWE", "Acme", null)
                )));

        ConnectionController controller = new ConnectionController(connectionService, userRepository);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockMvc.perform(get("/connections/2/mutual")
                        .principal(new UsernamePasswordAuthenticationToken("alice", null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].username").value("charlie"));

        verify(connectionService).getMutualConnections(eq(1L), eq(2L), any(PageRequest.class));

        mockMvc.perform(delete("/connections/55")
                        .principal(new UsernamePasswordAuthenticationToken("alice", null)))
                .andExpect(status().isOk());

        verify(connectionService).cancelOrRemoveConnection(55L, 1L);
    }
}
