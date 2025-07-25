package com.mybank.atmweb.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybank.atmweb.domain.User;
import com.mybank.atmweb.dto.SignupForm;
import com.mybank.atmweb.repository.UserRepository;
import com.mybank.atmweb.service.UserService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class UserApiControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserService userService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void 회원가입_성공() throws Exception {
        SignupForm dto = SignupForm.builder()
                .loginId("testLogin")
                .password("pass123123")
                .email("testUser@gmail.com")
                .name("testUser")
                .residentNumber("9008281647774")
                .gender("남성")
                .phoneNumber("01013245433")
                .build();

        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SIGNUP_SUCCESS"));

        Optional<User> savedUser = userRepository.findByLoginId(dto.getLoginId());
        assertThat(savedUser).isPresent();
        User user = savedUser.get();
        assertThat(user.getLoginId()).isEqualTo("testLogin");
        assertThat(passwordEncoder.matches(dto.getPassword(), user.getPassword()));
        assertThat(user.getName()).isEqualTo("testUser");
        assertThat(user.getResidentNumber()).isEqualTo("9008281647774");
        assertThat(user.getGender()).isEqualTo("남성");
        assertThat(user.getPhoneNumber()).isEqualTo("01013245433");
    }
}
