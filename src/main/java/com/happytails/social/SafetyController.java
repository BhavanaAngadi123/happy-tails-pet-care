package com.happytails.social;

import jakarta.persistence.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name="pet_blocks",uniqueConstraints=@UniqueConstraint(columnNames={"blockerPetId","blockedPetId"}))
class PetBlock extends BaseEntity {
  Long blockerPetId; Long blockedPetId; LocalDateTime createdAt=LocalDateTime.now();
  public Long getBlockerPetId(){return blockerPetId;} public void setBlockerPetId(Long v){blockerPetId=v;}
  public Long getBlockedPetId(){return blockedPetId;} public void setBlockedPetId(Long v){blockedPetId=v;}
  public LocalDateTime getCreatedAt(){return createdAt;}
}

@Entity
@Table(name="pet_reports")
class PetReport extends BaseEntity {
  Long reporterPetId; Long reportedPetId; String reason;
  @Column(length=1500) String details;
  String status="OPEN"; LocalDateTime createdAt=LocalDateTime.now();
  public Long getReporterPetId(){return reporterPetId;} public void setReporterPetId(Long v){reporterPetId=v;}
  public Long getReportedPetId(){return reportedPetId;} public void setReportedPetId(Long v){reportedPetId=v;}
  public String getReason(){return reason;} public void setReason(String v){reason=v;}
  public String getDetails(){return details;} public void setDetails(String v){details=v;}
  public String getStatus(){return status;} public void setStatus(String v){status=v;}
  public LocalDateTime getCreatedAt(){return createdAt;}
}

interface PetBlockRepository extends JpaRepository<PetBlock,Long>{
  boolean existsByBlockerPetIdAndBlockedPetId(Long a,Long b);
  List<PetBlock> findByBlockerPetId(Long id);
  Optional<PetBlock> findByBlockerPetIdAndBlockedPetId(Long a,Long b);
}
interface PetReportRepository extends JpaRepository<PetReport,Long>{List<PetReport> findByReporterPetIdOrderByCreatedAtDesc(Long id);}

@RestController
@RequestMapping("/api/safety")
class SafetyController {
  private final PetBlockRepository blocks; private final PetReportRepository reports; private final PetProfileRepository profiles;
  SafetyController(PetBlockRepository blocks,PetReportRepository reports,PetProfileRepository profiles){this.blocks=blocks;this.reports=reports;this.profiles=profiles;}
  private Long active(HttpSession s){Object x=s.getAttribute("activePetId");return x instanceof Long?(Long)x:null;}
  private ResponseEntity<Map<String,String>> unauth(){return ResponseEntity.status(401).body(Map.of("error","Please log in first."));}

  @GetMapping("/blocked") ResponseEntity<?> blocked(HttpSession s){Long me=active(s);if(me==null)return unauth();return ResponseEntity.ok(blocks.findByBlockerPetId(me));}

  @PostMapping("/block/{petId}") ResponseEntity<?> block(@PathVariable Long petId,HttpSession s){Long me=active(s);if(me==null)return unauth();if(Objects.equals(me,petId))return ResponseEntity.badRequest().body(Map.of("error","You cannot block your own pet profile."));if(!profiles.existsById(petId))return ResponseEntity.notFound().build();if(blocks.existsByBlockerPetIdAndBlockedPetId(me,petId))return ResponseEntity.ok(Map.of("status","already-blocked"));PetBlock b=new PetBlock();b.setBlockerPetId(me);b.setBlockedPetId(petId);return ResponseEntity.status(201).body(blocks.save(b));}

  @DeleteMapping("/block/{petId}") ResponseEntity<?> unblock(@PathVariable Long petId,HttpSession s){Long me=active(s);if(me==null)return unauth();return blocks.findByBlockerPetIdAndBlockedPetId(me,petId).<ResponseEntity<?>>map(b->{blocks.delete(b);return ResponseEntity.ok(Map.of("status","unblocked"));}).orElseGet(()->ResponseEntity.notFound().build());}

  @PostMapping("/report/{petId}") ResponseEntity<?> report(@PathVariable Long petId,@RequestBody Map<String,String> req,HttpSession s){Long me=active(s);if(me==null)return unauth();if(Objects.equals(me,petId))return ResponseEntity.badRequest().body(Map.of("error","You cannot report your own pet profile."));if(!profiles.existsById(petId))return ResponseEntity.notFound().build();String reason=req.getOrDefault("reason","").trim();if(reason.isBlank())return ResponseEntity.badRequest().body(Map.of("error","Choose a report reason."));PetReport r=new PetReport();r.setReporterPetId(me);r.setReportedPetId(petId);r.setReason(reason);r.setDetails(req.getOrDefault("details","").trim());return ResponseEntity.status(201).body(reports.save(r));}
}
