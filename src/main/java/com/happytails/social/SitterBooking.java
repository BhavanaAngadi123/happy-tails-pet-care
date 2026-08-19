package com.happytails.social;

import jakarta.persistence.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.*;
import java.util.*;

@Entity
@Table(name="sitter_bookings")
class SitterBooking extends BaseEntity {
  Long petProfileId; Long sitterId;
  LocalDateTime startAt; LocalDateTime endAt;
  String status="PENDING";
  @Column(length=1200) String notes;
  LocalDateTime createdAt=LocalDateTime.now();
  public Long getPetProfileId(){return petProfileId;} public void setPetProfileId(Long v){petProfileId=v;}
  public Long getSitterId(){return sitterId;} public void setSitterId(Long v){sitterId=v;}
  public LocalDateTime getStartAt(){return startAt;} public void setStartAt(LocalDateTime v){startAt=v;}
  public LocalDateTime getEndAt(){return endAt;} public void setEndAt(LocalDateTime v){endAt=v;}
  public String getStatus(){return status;} public void setStatus(String v){status=v;}
  public String getNotes(){return notes;} public void setNotes(String v){notes=v;}
  public LocalDateTime getCreatedAt(){return createdAt;}
}

interface SitterBookingRepository extends JpaRepository<SitterBooking,Long>{
  List<SitterBooking> findByPetProfileIdOrderByStartAtDesc(Long petProfileId);
}

@RestController
@RequestMapping("/api/sitter-bookings")
class SitterBookingController {
  private final SitterBookingRepository bookings; private final PetSitterRepository sitters;
  SitterBookingController(SitterBookingRepository bookings,PetSitterRepository sitters){this.bookings=bookings;this.sitters=sitters;}
  private Long active(HttpSession s){Object x=s.getAttribute("activePetId");return x instanceof Long?(Long)x:null;}

  @GetMapping ResponseEntity<?> mine(HttpSession s){Long pet=active(s);if(pet==null)return ResponseEntity.status(401).body(Map.of("error","login required"));return ResponseEntity.ok(bookings.findByPetProfileIdOrderByStartAtDesc(pet));}

  @PostMapping ResponseEntity<?> create(@RequestBody SitterBooking b,HttpSession s){Long pet=active(s);if(pet==null)return ResponseEntity.status(401).body(Map.of("error","login required"));if(b.getSitterId()==null||!sitters.existsById(b.getSitterId()))return ResponseEntity.badRequest().body(Map.of("error","Sitter not found."));if(b.getStartAt()==null||b.getEndAt()==null||!b.getEndAt().isAfter(b.getStartAt()))return ResponseEntity.badRequest().body(Map.of("error","Choose a valid start and end time."));if(b.getStartAt().isBefore(LocalDateTime.now()))return ResponseEntity.badRequest().body(Map.of("error","Booking must be in the future."));b.setPetProfileId(pet);b.setStatus("PENDING");return ResponseEntity.status(201).body(bookings.save(b));}

  @PatchMapping("/{id}/cancel") ResponseEntity<?> cancel(@PathVariable Long id,HttpSession s){Long pet=active(s);if(pet==null)return ResponseEntity.status(401).build();return bookings.findById(id).<ResponseEntity<?>>map(b->{if(!Objects.equals(b.getPetProfileId(),pet))return ResponseEntity.status(403).body(Map.of("error","Not your booking."));if("CANCELLED".equals(b.getStatus()))return ResponseEntity.ok(b);b.setStatus("CANCELLED");return ResponseEntity.ok(bookings.save(b));}).orElseGet(()->ResponseEntity.notFound().build());}

  @PatchMapping("/{id}/confirm-demo") ResponseEntity<?> confirmDemo(@PathVariable Long id,HttpSession s){Long pet=active(s);if(pet==null)return ResponseEntity.status(401).build();return bookings.findById(id).<ResponseEntity<?>>map(b->{if(!Objects.equals(b.getPetProfileId(),pet))return ResponseEntity.status(403).body(Map.of("error","Not your booking."));if(!"PENDING".equals(b.getStatus()))return ResponseEntity.badRequest().body(Map.of("error","Only pending requests can be confirmed."));b.setStatus("CONFIRMED");return ResponseEntity.ok(bookings.save(b));}).orElseGet(()->ResponseEntity.notFound().build());}
}
