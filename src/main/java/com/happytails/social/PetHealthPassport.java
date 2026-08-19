package com.happytails.social;

import jakarta.persistence.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.*;
import java.util.*;

@Entity
@Table(name="pet_health_profiles", uniqueConstraints=@UniqueConstraint(columnNames="petProfileId"))
class PetHealthProfile extends BaseEntity {
  Long petProfileId;
  @Column(length=1500) String allergies;
  @Column(length=1500) String conditions;
  String primaryVet; String vetPhone; String emergencyContact; String emergencyPhone;
  public Long getPetProfileId(){return petProfileId;} public void setPetProfileId(Long v){petProfileId=v;}
  public String getAllergies(){return allergies;} public void setAllergies(String v){allergies=v;}
  public String getConditions(){return conditions;} public void setConditions(String v){conditions=v;}
  public String getPrimaryVet(){return primaryVet;} public void setPrimaryVet(String v){primaryVet=v;}
  public String getVetPhone(){return vetPhone;} public void setVetPhone(String v){vetPhone=v;}
  public String getEmergencyContact(){return emergencyContact;} public void setEmergencyContact(String v){emergencyContact=v;}
  public String getEmergencyPhone(){return emergencyPhone;} public void setEmergencyPhone(String v){emergencyPhone=v;}
}

@Entity
@Table(name="pet_health_records")
class PetHealthRecord extends BaseEntity {
  Long petProfileId;
  String category; String title; String provider;
  @Column(length=2500) String notes;
  LocalDate eventDate; LocalDate nextDueDate;
  String status="COMPLETED";
  LocalDateTime createdAt=LocalDateTime.now();
  public Long getPetProfileId(){return petProfileId;} public void setPetProfileId(Long v){petProfileId=v;}
  public String getCategory(){return category;} public void setCategory(String v){category=v;}
  public String getTitle(){return title;} public void setTitle(String v){title=v;}
  public String getProvider(){return provider;} public void setProvider(String v){provider=v;}
  public String getNotes(){return notes;} public void setNotes(String v){notes=v;}
  public LocalDate getEventDate(){return eventDate;} public void setEventDate(LocalDate v){eventDate=v;}
  public LocalDate getNextDueDate(){return nextDueDate;} public void setNextDueDate(LocalDate v){nextDueDate=v;}
  public String getStatus(){return status;} public void setStatus(String v){status=v;}
  public LocalDateTime getCreatedAt(){return createdAt;}
}

@Entity
@Table(name="pet_medications")
class PetMedication extends BaseEntity {
  Long petProfileId; String name; String dose; String frequency;
  LocalDate startDate; LocalDate endDate;
  @Column(length=1500) String notes;
  boolean active=true;
  public Long getPetProfileId(){return petProfileId;} public void setPetProfileId(Long v){petProfileId=v;}
  public String getName(){return name;} public void setName(String v){name=v;}
  public String getDose(){return dose;} public void setDose(String v){dose=v;}
  public String getFrequency(){return frequency;} public void setFrequency(String v){frequency=v;}
  public LocalDate getStartDate(){return startDate;} public void setStartDate(LocalDate v){startDate=v;}
  public LocalDate getEndDate(){return endDate;} public void setEndDate(LocalDate v){endDate=v;}
  public String getNotes(){return notes;} public void setNotes(String v){notes=v;}
  public boolean isActive(){return active;} public void setActive(boolean v){active=v;}
}

interface PetHealthProfileRepository extends JpaRepository<PetHealthProfile,Long>{Optional<PetHealthProfile> findByPetProfileId(Long petProfileId);}
interface PetHealthRecordRepository extends JpaRepository<PetHealthRecord,Long>{List<PetHealthRecord> findByPetProfileIdOrderByEventDateDesc(Long petProfileId);}
interface PetMedicationRepository extends JpaRepository<PetMedication,Long>{List<PetMedication> findByPetProfileIdOrderByActiveDescStartDateDesc(Long petProfileId);}

@RestController
@RequestMapping("/api/health-passport")
class PetHealthPassportController {
  private final PetHealthProfileRepository profiles;
  private final PetHealthRecordRepository records;
  private final PetMedicationRepository medications;
  private final PetReminderRepository reminders;
  PetHealthPassportController(PetHealthProfileRepository profiles,PetHealthRecordRepository records,PetMedicationRepository medications,PetReminderRepository reminders){this.profiles=profiles;this.records=records;this.medications=medications;this.reminders=reminders;}
  private Long active(HttpSession s){Object x=s.getAttribute("activePetId");return x instanceof Long?(Long)x:null;}
  private ResponseEntity<Map<String,String>> login(){return ResponseEntity.status(401).body(Map.of("error","Please log in and select a pet."));}

  @GetMapping public ResponseEntity<?> passport(HttpSession s){Long pet=active(s);if(pet==null)return login();Map<String,Object> out=new LinkedHashMap<>();out.put("profile",profiles.findByPetProfileId(pet).orElse(null));out.put("records",records.findByPetProfileIdOrderByEventDateDesc(pet));out.put("medications",medications.findByPetProfileIdOrderByActiveDescStartDateDesc(pet));out.put("reminders",reminders.findByPetProfileIdOrderByDueDateAsc(pet));return ResponseEntity.ok(out);}

  @PutMapping("/profile") public ResponseEntity<?> saveProfile(@RequestBody PetHealthProfile incoming,HttpSession s){Long pet=active(s);if(pet==null)return login();PetHealthProfile p=profiles.findByPetProfileId(pet).orElseGet(PetHealthProfile::new);p.setPetProfileId(pet);p.setAllergies(clean(incoming.getAllergies()));p.setConditions(clean(incoming.getConditions()));p.setPrimaryVet(clean(incoming.getPrimaryVet()));p.setVetPhone(clean(incoming.getVetPhone()));p.setEmergencyContact(clean(incoming.getEmergencyContact()));p.setEmergencyPhone(clean(incoming.getEmergencyPhone()));return ResponseEntity.ok(profiles.save(p));}

  @PostMapping("/records") public ResponseEntity<?> addRecord(@RequestBody PetHealthRecord r,HttpSession s){Long pet=active(s);if(pet==null)return login();if(r.getTitle()==null||r.getTitle().isBlank()||r.getCategory()==null||r.getCategory().isBlank())return ResponseEntity.badRequest().body(Map.of("error","Category and title are required."));r.setPetProfileId(pet);if(r.getEventDate()==null)r.setEventDate(LocalDate.now());PetHealthRecord saved=records.save(r);if(r.getNextDueDate()!=null){PetReminder reminder=new PetReminder();reminder.setPetProfileId(pet);reminder.setType(r.getCategory().toUpperCase());reminder.setTitle("Next: "+r.getTitle());reminder.setDueDate(r.getNextDueDate());reminders.save(reminder);}return ResponseEntity.status(201).body(saved);}

  @DeleteMapping("/records/{id}") public ResponseEntity<?> deleteRecord(@PathVariable Long id,HttpSession s){Long pet=active(s);if(pet==null)return login();return records.findById(id).map(r->{if(!Objects.equals(r.getPetProfileId(),pet))return ResponseEntity.status(403).body(Map.of("error","Not allowed."));records.delete(r);return ResponseEntity.ok(Map.of("deleted",true));}).orElse(ResponseEntity.notFound().build());}

  @PostMapping("/medications") public ResponseEntity<?> addMedication(@RequestBody PetMedication m,HttpSession s){Long pet=active(s);if(pet==null)return login();if(m.getName()==null||m.getName().isBlank())return ResponseEntity.badRequest().body(Map.of("error","Medication name is required."));m.setPetProfileId(pet);return ResponseEntity.status(201).body(medications.save(m));}

  @PatchMapping("/medications/{id}/stop") public ResponseEntity<?> stopMedication(@PathVariable Long id,HttpSession s){Long pet=active(s);if(pet==null)return login();return medications.findById(id).map(m->{if(!Objects.equals(m.getPetProfileId(),pet))return ResponseEntity.status(403).body(Map.of("error","Not allowed."));m.setActive(false);if(m.getEndDate()==null)m.setEndDate(LocalDate.now());return ResponseEntity.ok(medications.save(m));}).orElse(ResponseEntity.notFound().build());}

  private String clean(String v){return v==null?null:v.trim();}
}
