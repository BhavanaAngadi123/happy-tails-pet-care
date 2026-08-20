package com.happytails.social;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.*;
import java.util.*;

@RestController
@RequestMapping("/api/notifications")
class NotificationController {
  private final FriendRequestRepository requests;
  private final PetMessageRepository messages;
  private final PlayDateRepository playDates;
  private final PetReminderRepository reminders;
  private final SitterBookingRepository sitterBookings;
  private final PetProfileRepository profiles;
  private final PetSitterRepository sitters;
  private final PetBlockRepository blocks;

  NotificationController(FriendRequestRepository requests,PetMessageRepository messages,PlayDateRepository playDates,
      PetReminderRepository reminders,SitterBookingRepository sitterBookings,PetProfileRepository profiles,
      PetSitterRepository sitters,PetBlockRepository blocks){
    this.requests=requests;this.messages=messages;this.playDates=playDates;this.reminders=reminders;
    this.sitterBookings=sitterBookings;this.profiles=profiles;this.sitters=sitters;this.blocks=blocks;
  }

  private Long active(HttpSession s){Object x=s.getAttribute("activePetId");return x instanceof Long?(Long)x:null;}
  private boolean blocked(Long a,Long b){return a!=null&&b!=null&&(blocks.existsByBlockerPetIdAndBlockedPetId(a,b)||blocks.existsByBlockerPetIdAndBlockedPetId(b,a));}
  private String petName(Long id){return profiles.findById(id).map(PetProfile::getName).orElse("A pet");}
  private Map<String,Object> item(String id,String type,String title,String detail,String view,Object when,boolean urgent){Map<String,Object> m=new LinkedHashMap<>();m.put("id",id);m.put("type",type);m.put("title",title);m.put("detail",detail);m.put("view",view);m.put("when",when);m.put("urgent",urgent);return m;}

  @GetMapping
  ResponseEntity<?> notifications(HttpSession s){
    Long me=active(s);if(me==null)return ResponseEntity.status(401).body(Map.of("error","Please log in and select a pet."));
    List<Map<String,Object>> out=new ArrayList<>();

    for(FriendRequest r:requests.findByToPetIdAndStatus(me,"PENDING")){
      if(blocked(me,r.getFromPetId()))continue;
      out.add(item("friend-"+r.getId(),"FRIEND_REQUEST",petName(r.getFromPetId())+" sent a pet-friend request","Accept or decline the connection request.","friends",r.getCreatedAt(),true));
    }

    for(PetMessage m:messages.findByToPetIdAndReadFlagFalseOrderByCreatedAtDesc(me)){
      if(blocked(me,m.getFromPetId()))continue;
      String preview=m.getBody()==null?"New message":m.getBody().trim();if(preview.length()>70)preview=preview.substring(0,70)+"…";
      out.add(item("message-"+m.getId(),"MESSAGE","New message from "+petName(m.getFromPetId()),preview,"messages",m.getCreatedAt(),true));
    }

    for(PlayDate p:playDates.findByHostPetIdOrGuestPetIdOrderByScheduledAtAsc(me,me)){
      if(!Objects.equals(me,p.getGuestPetId())||!"PENDING".equalsIgnoreCase(p.getStatus())||blocked(me,p.getHostPetId()))continue;
      out.add(item("playdate-"+p.getId(),"PLAY_DATE",petName(p.getHostPetId())+" invited your pet to a play date",(p.getLocation()==null?"Location to be confirmed":p.getLocation()),"playdates",p.getScheduledAt(),true));
    }

    LocalDate today=LocalDate.now(),soon=today.plusDays(14);
    for(PetReminder r:reminders.findByPetProfileIdOrderByDueDateAsc(me)){
      if(r.isCompleted()||r.getDueDate()==null||r.getDueDate().isAfter(soon))continue;
      boolean overdue=r.getDueDate().isBefore(today);
      out.add(item("reminder-"+r.getId(),"REMINDER",overdue?"Overdue care reminder":"Upcoming care reminder",r.getTitle()+" · "+r.getDueDate(),"reminders",r.getDueDate(),overdue));
    }

    LocalDateTime now=LocalDateTime.now();
    for(SitterBooking b:sitterBookings.findByPetProfileIdOrderByStartAtDesc(me)){
      if(b.getStartAt()==null||b.getEndAt()==null||b.getEndAt().isBefore(now)||"CANCELLED".equalsIgnoreCase(b.getStatus()))continue;
      String sitter=sitters.findById(b.getSitterId()).map(PetSitter::getName).orElse("Pet sitter");
      if("CONFIRMED".equalsIgnoreCase(b.getStatus()))out.add(item("sitter-"+b.getId(),"SITTER","Sitter booking confirmed",sitter+" · "+b.getStartAt(),"sitters",b.getStartAt(),false));
      else if("PENDING".equalsIgnoreCase(b.getStatus()))out.add(item("sitter-"+b.getId(),"SITTER","Sitter request pending",sitter+" · "+b.getStartAt(),"sitters",b.getStartAt(),false));
    }

    out.sort((a,b)->String.valueOf(b.get("when")).compareTo(String.valueOf(a.get("when"))));
    long urgent=out.stream().filter(x->Boolean.TRUE.equals(x.get("urgent"))).count();
    return ResponseEntity.ok(Map.of("count",out.size(),"urgentCount",urgent,"items",out));
  }
}
