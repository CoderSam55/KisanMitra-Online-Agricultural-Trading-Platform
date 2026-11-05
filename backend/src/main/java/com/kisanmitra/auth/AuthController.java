package com.kisanmitra.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.kisanmitra.auth.SupabaseAuthService.SupabaseAuthException;
import com.kisanmitra.user.User;
import com.kisanmitra.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserRepository users;
    private final JwtService jwt;
    private final PasswordEncoder encoder;

    public record RegisterRequest(String name, String email, String password, String phone,
                                  String userType, String preferredLanguage, Map<String, Object> location) {}
    public record LoginRequest(String email, String password) {}

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        String email = normalizeEmail(request.email());
        validateCredentials(email, request.password());

        if (users.findByEmail(email).isPresent()) return ResponseEntity.status(409).body(Map.of("success", false, "message", "Email already registered."));
        User user = new User(); user.setName(firstNonBlank(request.name(), email)); user.setEmail(email); user.setPassword(encoder.encode(request.password())); user.setPhone(request.phone());
        if (request.userType()!=null && !request.userType().isBlank()) user.setUserType(request.userType());
        if (request.preferredLanguage()!=null) user.setPreferredLanguage(request.preferredLanguage());
        if (request.location()!=null) { user.setState(String.valueOf(request.location().getOrDefault("state", ""))); user.setDistrict(String.valueOf(request.location().getOrDefault("district", ""))); user.setTaluka(String.valueOf(request.location().getOrDefault("taluka", ""))); user.setVillage(String.valueOf(request.location().getOrDefault("village", ""))); }
        user = users.save(user);
        return ResponseEntity.status(201).body(successPayload(user));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        String email = normalizeEmail(request.email());
        validateCredentials(email, request.password());
        User user = users.findByEmail(email).orElseThrow(() -> new SupabaseAuthException(401, "Invalid email or password."));
        // Legacy profiles imported without a password are enrolled on their first local login.
        if (user.getPassword()==null || user.getPassword().isBlank()) { user.setPassword(encoder.encode(request.password())); users.save(user); }
        else if (!encoder.matches(request.password(), user.getPassword())) throw new SupabaseAuthException(401, "Invalid email or password.");
        return ResponseEntity.ok(successPayload(user));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            return ResponseEntity.status(401).body(Map.of("success", false));
        }
        return ResponseEntity.ok(Map.of("success", true, "data", Map.of("user", safe(user))));
    }

    @ExceptionHandler(SupabaseAuthException.class)
    public ResponseEntity<?> handleSupabaseError(SupabaseAuthException error) {
        return ResponseEntity.status(HttpStatusCode.valueOf(error.status()))
                .body(Map.of("success", false, "message", error.getMessage()));
    }

    private Map<String, Object> successPayload(User user) {
        return Map.of("success", true, "data", Map.of("user", safe(user), "token", jwt.generate(user.getEmail())));
    }

    private User findOrCreateProfile(JsonNode authUser, JsonNode metadata) {
        String authId = authUser.path("id").asText("");
        String email = normalizeEmail(authUser.path("email").asText(""));
        if (authId.isBlank() || email.isBlank()) {
            throw new SupabaseAuthException(502, "Supabase Auth returned an incomplete user profile.");
        }

        Optional<User> existing = users.findBySupabaseId(authId).or(() -> users.findByEmail(email));
        User user = existing.orElseGet(User::new);
        boolean newProfile = existing.isEmpty();
        user.setSupabaseId(authId);
        user.setEmail(email);
        user.setPassword(null);
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(firstNonBlank(metadata.path("name").asText(""), metadata.path("full_name").asText(""), email.substring(0, email.indexOf('@'))));
        }
        if (user.getPhone() == null || user.getPhone().isBlank()) {
            user.setPhone(firstNonBlank(metadata.path("phone").asText(""), authUser.path("phone").asText("")));
        }
        if (newProfile || user.getUserType() == null || user.getUserType().isBlank()) {
            user.setUserType(firstNonBlank(metadata.path("userType").asText(""), "farmer"));
        }
        if (newProfile || user.getPreferredLanguage() == null || user.getPreferredLanguage().isBlank()) {
            user.setPreferredLanguage(firstNonBlank(metadata.path("preferredLanguage").asText(""), "en"));
        }
        JsonNode location = metadata.path("location");
        if (newProfile || user.getState() == null) user.setState(location.path("state").asText(null));
        if (newProfile || user.getDistrict() == null) user.setDistrict(location.path("district").asText(null));
        if (newProfile || user.getTaluka() == null) user.setTaluka(location.path("taluka").asText(null));
        if (newProfile || user.getVillage() == null) user.setVillage(location.path("village").asText(null));
        return users.save(user);
    }

    private Map<String, Object> safe(User user) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", user.getId());
        result.put("name", user.getName());
        result.put("email", user.getEmail());
        result.put("phone", user.getPhone() == null ? "" : user.getPhone());
        result.put("userType", user.getUserType());
        result.put("role", user.getRole());
        result.put("preferredLanguage", user.getPreferredLanguage());
        return result;
    }

    private static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private static void validateCredentials(String email, String password) {
        if (email.isBlank() || password == null || password.isBlank()) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST, "Email and password are required.");
        }
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) if (value != null && !value.isBlank()) return value;
        return "";
    }

    private static void putIfPresent(Map<String, Object> target, String key, String value) {
        if (value != null && !value.isBlank()) target.put(key, value);
    }
}
