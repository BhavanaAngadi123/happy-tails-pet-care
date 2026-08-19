package com.happytails.social;

import jakarta.persistence.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name="owner_accounts")
class OwnerAccount {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
  @Column(nullable=false,unique=true) String email;
  @Column(nullable=false) String passwordHash;
  String displayName;
  LocalDateTime createdAt=LocalDateTime.now();
  public Long getId(){return id;} public String getEmail(){return email;} public void setEmail(String v){email=v;}
  public String getPasswordHash(){return passwordHash;} public void setPasswordHash(String v){passwordHash=v;}
  public String getDisplayName(){return displayName;} public void setDisplayName(String v){displayName=v;}
}

@Entity
@Table(name="owner_pet_links",uniqueConstraints=@UniqueConstraint(columnNames={"ownerId","petProfileId"}))
class OwnerPetLink {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
  Long ownerId; Long petProfileId;
  public Long getId(){return id;} public Long getOwnerId(){return ownerId;} public void setOwnerId(Long v){ownerId=v;}
  public Long getPetProfileId(){return petProfileId;} public void setPetProfileId(Long v){petProfileId=v;}
}

interface OwnerAccountRepository extends JpaRepository<OwnerAccount,Long>{Optional<OwnerAccount> findByEmailIgnoreCase(String email);boolean existsByEmailIgnoreCase(String email);}
interface OwnerPetLinkRepository extends JpaRepository<OwnerPetLink,Long>{List<OwnerPetLink> findByOwnerId(Long ownerId);boolean existsByOwnerIdAndPetProfileId(Long ownerId,Long petProfileId);}

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final OwnerAccountRepository owners; private final OwnerPetLinkRepository links; private final PetProfileRepository profiles;
  private final BCryptPasswordEncoder encoder=new BCryptPasswordEncoder(10);
  public AuthController(OwnerAccountRepository owners,OwnerPetLinkRepository links,PetProfileRepository profiles){this.owners=owners;this.links=links;this.profiles=profiles;}

  @PostMapping("/signup")
  public ResponseEntity<?> signup(@RequestBody Map<String,String> body,HttpSession session){
    String email=norm(body.get("email")), password=body.getOrDefault("password",""), name=body.getOrDefault("displayName","").trim();
    if(email.isBlank()||!email.contains("@"))return bad("Enter a valid email address.");
    if(password.length()<8)return bad("Password must be at least 8 characters.");
    if(owners.existsByEmailIgnoreCase(email))return ResponseEntity.status(409).body(Map.of("error","An account already exists for this email."));
    OwnerAccount o=new OwnerAccount();o.setEmail(email);o.setPasswordHash(encoder.encode(password));o.setDisplayName(name.isBlank()?"Pet Parent":name);o=owners.save(o);
    session.setAttribute("ownerId",o.getId());session.setMaxInactiveInterval(60*60*24*7);
    return ResponseEntity.status(201).body(sessionPayload(o,session));
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody Map<String,String> body,HttpSession session){
    String email=norm(body.get("email")), password=body.getOrDefault("password","");
    Optional<OwnerAccount> found=owners.findByEmailIgnoreCase(email);
    if(found.isEmpty()||!encoder.matches(password,found.get().getPasswordHash()))return ResponseEntity.status(401).body(Map.of("error","Incorrect email or password."));
    OwnerAccount o=found.get();session.setAttribute("ownerId",o.getId());session.setMaxInactiveInterval(60*60*24*7);
    List<PetProfile> pets=petsFor(o.getId());if(!pets.isEmpty()&&session.getAttribute("activePetId")==null)session.setAttribute("activePetId",pets.get(0).getId());
    return ResponseEntity.ok(sessionPayload(o,session));
  }

  @PostMapping("/logout") public Map<String,Object> logout(HttpSession session){session.invalidate();return Map.of("ok",true);}

  @GetMapping("/session")
  public ResponseEntity<?> current(HttpSession session){Long ownerId=ownerId(session);if(ownerId==null)return ResponseEntity.status(401).body(Map.of("loggedIn",false));return owners.findById(ownerId).<ResponseEntity<?>>map(o->ResponseEntity.ok(sessionPayload(o,session))).orElseGet(()->ResponseEntity.status(401).body(Map.of("loggedIn",false)));}

  @GetMapping("/pets")
  public ResponseEntity<?> pets(HttpSession session){Long ownerId=ownerId(session);if(ownerId==null)return unauthorized();return ResponseEntity.ok(petsFor(ownerId));}

  @PostMapping("/pets")
  public ResponseEntity<?> createPet(@RequestBody PetProfile p,HttpSession session){
    Long ownerId=ownerId(session);if(ownerId==null)return unauthorized();
    if(p.getName()==null||p.getName().isBlank()||p.getHandle()==null||p.getHandle().isBlank())return bad("Pet name and username are required.");
    String h=p.getHandle().trim();if(!h.startsWith("@"))h="@"+h;p.setHandle(h.toLowerCase());
    try{
      PetProfile saved=profiles.save(p);OwnerPetLink link=new OwnerPetLink();link.setOwnerId(ownerId);link.setPetProfileId(saved.getId());links.save(link);session.setAttribute("activePetId",saved.getId());return ResponseEntity.status(201).body(saved);
    }catch(DataIntegrityViolationException e){return ResponseEntity.status(409).body(Map.of("error","That pet username is already taken."));}
  }

  @PostMapping("/select-pet/{petId}")
  public ResponseEntity<?> selectPet(@PathVariable Long petId,HttpSession session){Long ownerId=ownerId(session);if(ownerId==null)return unauthorized();if(!links.existsByOwnerIdAndPetProfileId(ownerId,petId))return ResponseEntity.status(403).body(Map.of("error","This pet does not belong to your account."));session.setAttribute("activePetId",petId);return profiles.findById(petId).<ResponseEntity<?>>map(ResponseEntity::ok).orElseGet(()->ResponseEntity.notFound().build());}

  private Map<String,Object> sessionPayload(OwnerAccount o,HttpSession session){List<PetProfile> pets=petsFor(o.getId());Long active=(Long)session.getAttribute("activePetId");if(active==null&&!pets.isEmpty()){active=pets.get(0).getId();session.setAttribute("activePetId",active);}Map<String,Object> m=new LinkedHashMap<>();m.put("loggedIn",true);m.put("displayName",o.getDisplayName());m.put("email",o.getEmail());m.put("pets",pets);m.put("activePetId",active);return m;}
  private List<PetProfile> petsFor(Long ownerId){List<Long> ids=links.findByOwnerId(ownerId).stream().map(OwnerPetLink::getPetProfileId).toList();return ids.isEmpty()?List.of():profiles.findAllById(ids);}
  private Long ownerId(HttpSession s){Object x=s.getAttribute("ownerId");return x instanceof Long?(Long)x:null;}
  private String norm(String s){return s==null?"":s.trim().toLowerCase();}
  private ResponseEntity<Map<String,String>> bad(String s){return ResponseEntity.badRequest().body(Map.of("error",s));}
  private ResponseEntity<Map<String,String>> unauthorized(){return ResponseEntity.status(401).body(Map.of("error","Please log in first."));}
}
