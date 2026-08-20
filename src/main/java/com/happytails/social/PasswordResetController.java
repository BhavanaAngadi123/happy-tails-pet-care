package com.happytails.social;

import jakarta.persistence.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name="password_reset_tokens")
class PasswordResetToken {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
 @Column(nullable=false) Long ownerId;
 @Column(nullable=false,unique=true,length=64) String tokenHash;
 @Column(nullable=false) LocalDateTime expiresAt;
 @Column(nullable=false) boolean used=false;
 LocalDateTime createdAt=LocalDateTime.now();
}

interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken,Long>{
 Optional<PasswordResetToken> findByTokenHashAndUsedFalse(String tokenHash);
 List<PasswordResetToken> findByOwnerIdAndUsedFalse(Long ownerId);
}

@RestController
@RequestMapping("/api/auth/password-reset")
public class PasswordResetController {
 private final OwnerAccountRepository owners;
 private final PasswordResetTokenRepository tokens;
 private final ObjectProvider<JavaMailSender> mailSender;
 private final BCryptPasswordEncoder encoder=new BCryptPasswordEncoder(10);
 private final SecureRandom random=new SecureRandom();
 @Value("${happy-tails.password-reset.enabled:false}") boolean enabled;
 @Value("${happy-tails.password-reset.base-url:http://localhost:8080}") String baseUrl;
 @Value("${happy-tails.password-reset.from:no-reply@happytails.local}") String from;

 public PasswordResetController(OwnerAccountRepository owners,PasswordResetTokenRepository tokens,ObjectProvider<JavaMailSender> mailSender){this.owners=owners;this.tokens=tokens;this.mailSender=mailSender;}

 @GetMapping("/status") public Map<String,Object> status(){return Map.of("enabled",enabled&&mailSender.getIfAvailable()!=null);}

 @PostMapping("/request") public ResponseEntity<?> request(@RequestBody Map<String,String> body){
  String email=body.getOrDefault("email","").trim().toLowerCase(Locale.ROOT);
  if(enabled&&mailSender.getIfAvailable()!=null&&!email.isBlank())owners.findByEmailIgnoreCase(email).ifPresent(this::issueReset);
  return ResponseEntity.ok(Map.of("ok",true,"message","If an account exists for that email, reset instructions will be sent."));
 }

 @PostMapping("/confirm") public ResponseEntity<?> confirm(@RequestBody Map<String,String> body){
  if(!enabled||mailSender.getIfAvailable()==null)return ResponseEntity.status(503).body(Map.of("error","Password reset is not configured."));
  String token=body.getOrDefault("token","").trim(),password=body.getOrDefault("newPassword","");
  String e=passwordError(password);if(e!=null)return ResponseEntity.badRequest().body(Map.of("error",e));
  if(token.isBlank())return ResponseEntity.badRequest().body(Map.of("error","Reset token is required."));
  Optional<PasswordResetToken> found=tokens.findByTokenHashAndUsedFalse(hash(token));
  if(found.isEmpty()||found.get().expiresAt.isBefore(LocalDateTime.now()))return ResponseEntity.badRequest().body(Map.of("error","This reset link is invalid or expired."));
  PasswordResetToken t=found.get();OwnerAccount owner=owners.findById(t.ownerId).orElse(null);
  if(owner==null)return ResponseEntity.badRequest().body(Map.of("error","This reset link is invalid or expired."));
  owner.setPasswordHash(encoder.encode(password));owners.save(owner);
  tokens.findByOwnerIdAndUsedFalse(owner.getId()).forEach(x->{x.used=true;tokens.save(x);});
  return ResponseEntity.ok(Map.of("ok",true,"message","Password updated. You can now log in."));
 }

 private void issueReset(OwnerAccount owner){
  JavaMailSender sender=mailSender.getIfAvailable();if(sender==null)return;
  tokens.findByOwnerIdAndUsedFalse(owner.getId()).forEach(x->{x.used=true;tokens.save(x);});
  byte[] bytes=new byte[32];random.nextBytes(bytes);String raw=Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  PasswordResetToken t=new PasswordResetToken();t.ownerId=owner.getId();t.tokenHash=hash(raw);t.expiresAt=LocalDateTime.now().plusMinutes(30);tokens.save(t);
  String link=baseUrl.replaceAll("/$","")+"/login.html?resetToken="+raw;
  SimpleMailMessage msg=new SimpleMailMessage();msg.setFrom(from);msg.setTo(owner.getEmail());msg.setSubject("Reset your Happy Tails password");msg.setText("Use this link within 30 minutes to reset your Happy Tails password:\n\n"+link+"\n\nIf you did not request this, you can ignore this email.");
  try{sender.send(msg);}catch(Exception ex){t.used=true;tokens.save(t);}
 }

 private String hash(String s){try{byte[] d=MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));return HexFormat.of().formatHex(d);}catch(Exception e){throw new IllegalStateException(e);}}
 private String passwordError(String p){if(p==null||p.length()<10)return "Password must be at least 10 characters.";if(p.length()>128)return "Password is too long.";if(!p.matches(".*[A-Za-z].*"))return "Password must include a letter.";if(!p.matches(".*\\d.*"))return "Password must include a number.";return null;}
}
