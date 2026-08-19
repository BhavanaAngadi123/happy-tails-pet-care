package com.happytails.social;

import jakarta.persistence.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name="pet_messages", indexes={@Index(name="idx_pet_message_pair",columnList="fromPetId,toPetId")})
class PetMessage extends BaseEntity {
  Long fromPetId; Long toPetId;
  @Column(nullable=false,length=2000) String body;
  LocalDateTime createdAt=LocalDateTime.now();
  boolean readFlag=false;
  public PetMessage(){}
  public Long getFromPetId(){return fromPetId;} public void setFromPetId(Long v){fromPetId=v;}
  public Long getToPetId(){return toPetId;} public void setToPetId(Long v){toPetId=v;}
  public String getBody(){return body;} public void setBody(String v){body=v;}
  public LocalDateTime getCreatedAt(){return createdAt;}
  public boolean isReadFlag(){return readFlag;} public void setReadFlag(boolean v){readFlag=v;}
}

interface PetMessageRepository extends JpaRepository<PetMessage,Long>{
  List<PetMessage> findByFromPetIdAndToPetIdOrFromPetIdAndToPetIdOrderByCreatedAtAsc(Long a,Long b,Long c,Long d);
  List<PetMessage> findByToPetIdAndReadFlagFalseOrderByCreatedAtDesc(Long id);
  List<PetMessage> findByFromPetIdOrToPetIdOrderByCreatedAtDesc(Long a,Long b);
}

@RestController
@RequestMapping("/api/messages")
class PetMessageController {
  private final PetMessageRepository messages; private final PetProfileRepository profiles; private final PetFollowRepository follows;
  PetMessageController(PetMessageRepository messages,PetProfileRepository profiles,PetFollowRepository follows){this.messages=messages;this.profiles=profiles;this.follows=follows;}
  private Long active(HttpSession s){Object x=s.getAttribute("activePetId");return x instanceof Long?(Long)x:null;}

  @GetMapping("/inbox") ResponseEntity<?> inbox(HttpSession s){Long me=active(s);if(me==null)return ResponseEntity.status(401).body(Map.of("error","login required"));return ResponseEntity.ok(messages.findByFromPetIdOrToPetIdOrderByCreatedAtDesc(me,me));}
  @GetMapping("/unread") ResponseEntity<?> unread(HttpSession s){Long me=active(s);if(me==null)return ResponseEntity.status(401).body(Map.of("error","login required"));return ResponseEntity.ok(messages.findByToPetIdAndReadFlagFalseOrderByCreatedAtDesc(me));}
  @GetMapping("/with/{petId}") ResponseEntity<?> conversation(@PathVariable Long petId,HttpSession s){Long me=active(s);if(me==null)return ResponseEntity.status(401).body(Map.of("error","login required"));if(Objects.equals(me,petId))return ResponseEntity.badRequest().body(Map.of("error","Choose another pet."));return ResponseEntity.ok(messages.findByFromPetIdAndToPetIdOrFromPetIdAndToPetIdOrderByCreatedAtAsc(me,petId,petId,me));}
  @PostMapping("/with/{petId}") ResponseEntity<?> send(@PathVariable Long petId,@RequestBody Map<String,String> req,HttpSession s){Long me=active(s);if(me==null)return ResponseEntity.status(401).body(Map.of("error","login required"));if(Objects.equals(me,petId))return ResponseEntity.badRequest().body(Map.of("error","You cannot message your own pet."));Optional<PetProfile> target=profiles.findById(petId);if(target.isEmpty())return ResponseEntity.notFound().build();String permission=target.get().getMessagePermission()==null?"EVERYONE":target.get().getMessagePermission();if("NOBODY".equalsIgnoreCase(permission))return ResponseEntity.status(403).body(Map.of("error",target.get().getName()+" is not accepting new messages."));if("FOLLOWING".equalsIgnoreCase(permission)&&!follows.existsByFollowerPetIdAndFollowingPetId(petId,me))return ResponseEntity.status(403).body(Map.of("error","Only pets this account follows can send messages."));String body=req.getOrDefault("body","").trim();if(body.isEmpty())return ResponseEntity.badRequest().body(Map.of("error","Message cannot be empty."));PetMessage m=new PetMessage();m.setFromPetId(me);m.setToPetId(petId);m.setBody(body);return ResponseEntity.status(201).body(messages.save(m));}
  @PatchMapping("/with/{petId}/read") ResponseEntity<?> read(@PathVariable Long petId,HttpSession s){Long me=active(s);if(me==null)return ResponseEntity.status(401).build();List<PetMessage> list=messages.findByFromPetIdAndToPetIdOrFromPetIdAndToPetIdOrderByCreatedAtAsc(me,petId,petId,me);list.stream().filter(m->Objects.equals(m.getToPetId(),me)&&Objects.equals(m.getFromPetId(),petId)).forEach(m->m.setReadFlag(true));messages.saveAll(list);return ResponseEntity.ok(Map.of("status","read"));}
}
