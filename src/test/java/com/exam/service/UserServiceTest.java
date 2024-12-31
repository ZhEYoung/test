package com.exam.service;

import com.exam.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class UserServiceTest {

    @Autowired
    private UserService userService;

    private User testUser;
    private final String testUsername = "testUser";
    private final String testPassword = "testPass123";
    private final String testEmail = "test@example.com";
    private final String testPhone = "1234567890";

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername(testUsername);
        testUser.setPassword(testPassword);
        testUser.setRole(User.ROLE_STUDENT);
        testUser.setStatus(true);
        testUser.setSex(true);
        testUser.setEmail(testEmail);
        testUser.setPhone(testPhone);
    }

    @Test
    void testRegisterUser() {
        // Test successful registration
        int result = userService.register(testUser);
        assertEquals(1, result);

        // Verify user exists
        User savedUser = userService.getByUsername(testUsername);
        assertNotNull(savedUser);
        assertEquals(testUsername, savedUser.getUsername());
        assertTrue(savedUser.getStatus()); // User should be active by default
        
        // Test duplicate username
        User duplicateUser = new User();
        duplicateUser.setUsername(testUsername);
        duplicateUser.setPassword("different");
        duplicateUser.setRole(User.ROLE_STUDENT);
        assertEquals(0, userService.register(duplicateUser));
    }

    @Test
    void testLogin() {
        // Register user first
        userService.register(testUser);

        // Test successful login
        User loggedInUser = userService.login(testUsername, testPassword);
        assertNotNull(loggedInUser);
        assertEquals(testUsername, loggedInUser.getUsername());

        // Test wrong password
        assertNull(userService.login(testUsername, "wrongPassword"));

        // Test non-existent user
        assertNull(userService.login("nonExistentUser", testPassword));
    }

    @Test
    void testUpdatePassword() {
        // Register user first
        userService.register(testUser);
        
        String newPassword = "newPass123";
        
        // Test successful password update
        int result = userService.updatePassword(testUser.getUserId(), testPassword, newPassword);
        assertEquals(1, result);
        
        // Verify new password works for login
        assertNotNull(userService.login(testUsername, newPassword));
        assertNull(userService.login(testUsername, testPassword));
        
        // Test with wrong old password
        assertEquals(0, userService.updatePassword(testUser.getUserId(), "wrongPass", "anotherPass"));
    }

    @Test
    void testUpdateStatus() {
        // Register user first
        userService.register(testUser);
        
        // Test disabling user
        int result = userService.updateStatus(testUser.getUserId(), false);
        assertEquals(1, result);
        
        // Verify user is disabled
        User disabledUser = userService.getByUsername(testUsername);
        assertFalse(disabledUser.getStatus());
        
        // Test re-enabling user
        result = userService.updateStatus(testUser.getUserId(), true);
        assertEquals(1, result);
        
        // Verify user is enabled
        User enabledUser = userService.getByUsername(testUsername);
        assertTrue(enabledUser.getStatus());
    }

    @Test
    void testDeleteUser() {
        // Register user first
        userService.register(testUser);
        
        // Attempt to delete (should disable instead)
        int result = userService.deleteById(testUser.getUserId());
        assertEquals(1, result);
        
        // Verify user still exists but is disabled
        User disabledUser = userService.getByUsername(testUsername);
        assertNotNull(disabledUser);
        assertFalse(disabledUser.getStatus());
    }

    @Test
    void testUpdateContact() {
        // Register user first
        userService.register(testUser);
        
        String newPhone = "9876543210";
        String newEmail = "new@example.com";
        
        // Test updating contact information
        int result = userService.updateContact(testUser.getUserId(), newPhone, newEmail);
        assertEquals(1, result);
        
        // Verify contact information is updated
        User updatedUser = userService.getByUsername(testUsername);
        assertEquals(newPhone, updatedUser.getPhone());
        assertEquals(newEmail, updatedUser.getEmail());
    }

    @Test
    void testBatchUpdateStatus() {
        // Register multiple users
        User user1 = new User();
        user1.setUsername("user1");
        user1.setPassword("pass1");
        user1.setRole(User.ROLE_STUDENT);
        userService.register(user1);

        User user2 = new User();
        user2.setUsername("user2");
        user2.setPassword("pass2");
        user2.setRole(User.ROLE_STUDENT);
        userService.register(user2);

        List<Integer> userIds = Arrays.asList(user1.getUserId(), user2.getUserId());
        
        // Test batch disable
        int result = userService.batchUpdateStatus(userIds, false);
        assertTrue(result > 0);
        
        // Verify all users are disabled
        assertFalse(userService.getByUsername("user1").getStatus());
        assertFalse(userService.getByUsername("user2").getStatus());
    }

    @Test
    void testGetByRole() {
        // Register users with different roles
        User studentUser = testUser; // already set as ROLE_STUDENT
        userService.register(studentUser);

        User teacherUser = new User();
        teacherUser.setUsername("teacher1");
        teacherUser.setPassword("teacherPass");
        teacherUser.setRole(User.ROLE_TEACHER);
        userService.register(teacherUser);

        // Test getting users by role
        List<User> students = userService.getByRole(User.ROLE_STUDENT);
        List<User> teachers = userService.getByRole(User.ROLE_TEACHER);

        assertTrue(students.stream().anyMatch(u -> u.getUsername().equals(testUsername)));
        assertTrue(teachers.stream().anyMatch(u -> u.getUsername().equals("teacher1")));
    }

    @Test
    void testInvalidUserData() {
        // Test with null user
        assertEquals(0, userService.register(null));

        // Test with invalid username (too long)
        User invalidUser = new User();
        invalidUser.setUsername("thisUsernameIsTooLongForTheSystem");
        invalidUser.setPassword(testPassword);
        invalidUser.setRole(User.ROLE_STUDENT);
        assertEquals(0, userService.register(invalidUser));

        // Test with invalid role
        invalidUser.setUsername("validName");
        invalidUser.setRole(999); // Invalid role
        assertEquals(0, userService.register(invalidUser));
    }
} 