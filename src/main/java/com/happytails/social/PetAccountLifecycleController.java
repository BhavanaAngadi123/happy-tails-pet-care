package com.happytails.social;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@Service
class PetAccountCleanupService {
  @PersistenceContext EntityManager em;

  @Transactional
  public void deletePet(Long petId){
    delete("post_paws","pet_profile_id",petId);
    delete("post_paws","petProfileId",petId);
    delete("post_comments","pet_profile_id",petId);
    delete("post_comments","petProfileId",petId);
    delete("pet_community_posts","pet_profile_id",petId);
    delete("pet_community_posts","petProfileId",petId);
    delete("pet_community_members","pet_profile_id",petId);
    delete("pet_community_members","petProfileId",petId);
    deleteEither("pet_messages","from_pet_id","to_pet_id",petId);
    deleteEither("pet_messages","fromPetId","toPetId",petId);
    deleteEither("pet_blocks","blocker_pet_id","blocked_pet_id",petId);
    deleteEither("pet_blocks","blockerPetId","blockedPetId",petId);
    deleteEither("pet_reports","reporter_pet_id","reported_pet_id",petId);
    deleteEither("pet_reports","reporterPetId","reportedPetId",petId);
    deleteEither("pet_follows","follower_pet_id","following_pet_id",petId);
    deleteEither("pet_follows","followerPetId","followingPetId",petId);
    deleteEither("friend_requests","from_pet_id","to_pet_id",petId);
    deleteEither("friend_requests","fromPetId","toPetId",petId);
    deleteEither("play_dates","host_pet_id","guest_pet_id",petId);
    deleteEither("play_dates","hostPetId","guestPetId",petId);
    delete("meetup_attendees","pet_profile_id",petId);
    delete("meetup_attendees","petProfileId",petId);
    delete("pet_reminders","pet_profile_id",petId);
    delete("pet_reminders","petProfileId",petId);
    delete("pet_orders","pet_profile_id",petId);
    delete("pet_orders","petProfileId",petId);
    delete("pet_memories","pet_profile_id",petId);
    delete("pet_memories","petProfileId",petId);
    delete("pet_health_profiles","pet_profile_id",petId);
    delete("pet_health_profiles","petProfileId",petId);
    delete("pet_health_records","pet_profile_id",petId);
    delete("pet_health_records","petProfileId",petId);
    delete("pet_medications","pet_profile_id",petId);
    delete("pet_medications","petProfileId",petId);
    delete("sitter_bookings","pet_profile_id",petId);
    delete("sitter_bookings","petProfileId",petId);
    delete("social_posts","pet_profile_id",petId);
    delete("social_posts","petProfileId",petId);
    delete("owner_pet_links","pet_profile_id",petId);
    delete("owner_pet_links","petProfileId",petId);
    try{em.createNativeQuery("delete from pet_profiles where id=:id").setParameter("id",petId).executeUpdate();}catch(Exception ignored){}
    recalcSocialCounts();
  }

  private void delete(String table,String column,Long id){try{em.createNativeQuery("delete from "+table+" where "+column+"=:id").setParameter("id",id).executeUpdate();em.flush();}catch(Exception ignored){}}
  private void deleteEither(String table,String a,String b,Long id){try{em.createNativeQuery("delete from "+table+" where "+a+"=:id or "+b+"=:id").setParameter("id",id).executeUpdate();em.flush();}catch(Exception ignored){}}
  private void recalcSocialCounts(){
    try{em.createNativeQuery("update pet_profiles p set followers=(select count(*) from pet_follows f where f.following_pet_id=p.id), following=(select count(*) from pet_follows f where f.follower_pet_id=p.id)").executeUpdate();}catch(Exception first){try{em.createNativeQuery("update pet_profiles p set followers=(select count(*) from pet_follows f where f.followingPetId=p.id), following=(select count(*) from pet_follows f where f.followerPetId=p.id)").executeUpdate();}catch(Exception ignored){}}
  }
}

@RestController
@RequestMapping("/api/auth")
class PetAccountLifecycleController {
  private final OwnerPetLinkRepository links; private final PetProfileRepository profiles; private final PetAccountCleanupService cleanup;
  PetAccountLifecycleController(OwnerPetLinkRepository links,PetProfileRepository profiles,PetAccountCleanupService cleanup){this.links=links;this.profiles=profiles;this.cleanup=cleanup;}
  private Long owner(HttpSession s){Object x=s.getAttribute("ownerId");return x instanceof Long?(Long)x:null;}
  private Long active(HttpSession s){Object x=s.getAttribute("activePetId");return x instanceof Long?(Long)x:null;}

  @DeleteMapping("/pets/{petId}")
  ResponseEntity<?> deletePet(@PathVariable Long petId,@RequestBody(required=false) Map<String,String> body,HttpSession session){
    Long ownerId=owner(session);if(ownerId==null)return ResponseEntity.status(401).body(Map.of("error","Please log in first."));
    if(!links.existsByOwnerIdAndPetProfileId(ownerId,petId))return ResponseEntity.status(403).body(Map.of("error","This pet does not belong to your account."));
    PetProfile pet=profiles.findById(petId).orElse(null);if(pet==null)return ResponseEntity.notFound().build();
    String confirm=body==null?"":body.getOrDefault("confirmName","").trim();if(!pet.getName().equalsIgnoreCase(confirm))return ResponseEntity.badRequest().body(Map.of("error","Type the pet's name to confirm permanent deletion."));
    cleanup.deletePet(petId);
    List<OwnerPetLink> remaining=links.findByOwnerId(ownerId);Long next=null;for(OwnerPetLink l:remaining){if(profiles.existsById(l.getPetProfileId())){next=l.getPetProfileId();break;}}
    if(Objects.equals(active(session),petId)){if(next==null)session.removeAttribute("activePetId");else session.setAttribute("activePetId",next);}
    return ResponseEntity.ok(Map.of("deleted",true,"nextPetId",next==null?0L:next,"hasPets",next!=null));
  }
}
