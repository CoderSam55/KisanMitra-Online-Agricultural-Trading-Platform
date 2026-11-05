package com.kisanmitra.auth;

import com.kisanmitra.user.*; import jakarta.validation.Valid; import jakarta.validation.constraints.*; import lombok.RequiredArgsConstructor;
import org.springframework.http.*; import org.springframework.security.crypto.password.PasswordEncoder; import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController @RequestMapping("/api/auth") @RequiredArgsConstructor
public class AuthController {
  private final UserRepository users; private final PasswordEncoder encoder; private final JwtService jwt;
  private Map<String,Object> safe(User u){ return Map.of("id",u.getId(),"name",u.getName(),"email",u.getEmail(),"phone",u.getPhone()==null?"":u.getPhone(),"userType",u.getUserType(),"role",u.getRole()); }
  public record RegisterRequest(@NotBlank String name,@Email @NotBlank String email,@Size(min=6) String password,String phone,String userType){}
  public record LoginRequest(@Email @NotBlank String email,@NotBlank String password){}
  @PostMapping("/register") public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest r){
    if(users.findByEmail(r.email().toLowerCase()).isPresent()) return ResponseEntity.status(409).body(Map.of("success",false,"message","Email already registered"));
    User u=new User(); u.setName(r.name()); u.setEmail(r.email().toLowerCase()); u.setPassword(encoder.encode(r.password())); u.setPhone(r.phone()); if(r.userType()!=null)u.setUserType(r.userType()); users.save(u);
    return ResponseEntity.status(201).body(Map.of("success",true,"data",Map.of("user",safe(u),"token",jwt.generate(u.getEmail()))));
  }
  @PostMapping("/login") public ResponseEntity<?> login(@Valid @RequestBody LoginRequest r){
    var u=users.findByEmail(r.email().toLowerCase()); if(u.isEmpty()||!encoder.matches(r.password(),u.get().getPassword())) return ResponseEntity.status(401).body(Map.of("success",false,"message","Invalid email or password"));
    return ResponseEntity.ok(Map.of("success",true,"data",Map.of("user",safe(u.get()),"token",jwt.generate(u.get().getEmail()))));
  }
  @GetMapping("/me") public ResponseEntity<?> me(org.springframework.security.core.Authentication a){ if(a==null||!(a.getPrincipal() instanceof User u)) return ResponseEntity.status(401).body(Map.of("success",false)); return ResponseEntity.ok(Map.of("success",true,"data",Map.of("user",safe(u)))); }
}
