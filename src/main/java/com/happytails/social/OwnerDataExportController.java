package com.happytails.social;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/auth")
public class OwnerDataExportController {
 private final OwnerAccountRepository owners;
 private final OwnerPetLinkRepository links;
 private final PetProfileRepository profiles;
 private final JdbcTemplate jdbc;

 public OwnerDataExportController(OwnerAccountRepository owners,OwnerPetLinkRepository links,PetProfileRepository profiles,JdbcTemplate jdbc){
  this.owners=owners;this.links=links;this.profiles=profiles;this.jdbc=jdbc;
 }

 @GetMapping(value="/export",produces=MediaType.APPLICATION_JSON_VALUE)
 public ResponseEntity<?> export(HttpSession session){
  Long ownerId=owner(session);if(ownerId==null)return ResponseEntity.status(401).body(Map.of("error","Please log in first."));
  OwnerAccount owner=owners.findById(ownerId).orElse(null);if(owner==null)return ResponseEntity.status(401).body(Map.of("error","Please log in first."));
  List<Long> petIds=links.findByOwnerId(ownerId).stream().map(OwnerPetLink::getPetProfileId).filter(Objects::nonNull).toList();

  Map<String,Object> out=new LinkedHashMap<>();
  out.put("format","happy-tails-owner-export-v1");
  out.put("exportedAt",Instant.now().toString());
  out.put("owner",Map.of("email",owner.getEmail(),"displayName",owner.getDisplayName()==null?"":owner.getDisplayName()));
  out.put("pets",petIds.isEmpty()?List.of():profiles.findAllById(petIds));

  Map<String,Object> data=new LinkedHashMap<>();
  if(!petIds.isEmpty()){
   data.put("posts",byPet("social_posts","pet_profile_id",petIds));
   data.put("paws",byPetOrPost("post_paws","pet_profile_id",petIds));
   data.put("comments",byPetOrPost("post_comments","pet_profile_id",petIds));
   data.put("follows",either("pet_follows","follower_pet_id","following_pet_id",petIds));
   data.put("friendRequests",either("friend_requests","from_pet_id","to_pet_id",petIds));
   data.put("messages",either("pet_messages","from_pet_id","to_pet_id",petIds));
   data.put("blocks",either("pet_blocks","blocker_pet_id","blocked_pet_id",petIds));
   data.put("reports",either("pet_reports","reporter_pet_id","reported_pet_id",petIds));
   data.put("playDates",either("play_dates","host_pet_id","guest_pet_id",petIds));
   data.put("meetupAttendance",byPet("meetup_attendees","pet_profile_id",petIds));
   data.put("communityMemberships",byPet("pet_community_members","pet_profile_id",petIds));
   data.put("communityPosts",byPet("pet_community_posts","pet_profile_id",petIds));
   data.put("reminders",byPet("pet_reminders","pet_profile_id",petIds));
   data.put("orders",byPet("pet_orders","pet_profile_id",petIds));
   data.put("memories",byPet("pet_memories","pet_profile_id",petIds));
   data.put("healthProfiles",byPet("pet_health_profiles","pet_profile_id",petIds));
   data.put("healthRecords",byPet("pet_health_records","pet_profile_id",petIds));
   data.put("medications",byPet("pet_medications","pet_profile_id",petIds));
   data.put("sitterBookings",byPet("sitter_bookings","pet_profile_id",petIds));
  }
  out.put("data",data);
  return ResponseEntity.ok()
    .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=happy-tails-data.json")
    .header(HttpHeaders.CACHE_CONTROL,"no-store")
    .body(out);
 }

 private List<Map<String,Object>> byPet(String table,String column,List<Long> ids){
  return safe("select * from "+table+" where "+column+" in ("+marks(ids.size())+")",ids);
 }
 private List<Map<String,Object>> either(String table,String a,String b,List<Long> ids){
  List<Object> args=new ArrayList<>();args.addAll(ids);args.addAll(ids);
  return safe("select * from "+table+" where "+a+" in ("+marks(ids.size())+") or "+b+" in ("+marks(ids.size())+")",args);
 }
 private List<Map<String,Object>> byPetOrPost(String table,String petColumn,List<Long> ids){
  List<Object> args=new ArrayList<>();args.addAll(ids);args.addAll(ids);
  String sql="select * from "+table+" where "+petColumn+" in ("+marks(ids.size())+") or post_id in (select id from social_posts where pet_profile_id in ("+marks(ids.size())+"))";
  return safe(sql,args);
 }
 private List<Map<String,Object>> safe(String sql,List<?> args){
  try{return jdbc.queryForList(sql,args.toArray());}catch(Exception ignored){return List.of();}
 }
 private String marks(int n){return String.join(",",Collections.nCopies(n,"?"));}
 private Long owner(HttpSession s){if(s==null)return null;Object x=s.getAttribute("ownerId");return x instanceof Long?(Long)x:null;}
}
