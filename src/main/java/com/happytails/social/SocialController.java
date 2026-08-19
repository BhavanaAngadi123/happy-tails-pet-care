package com.happytails.social;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/social")
@CrossOrigin
public class SocialController {
  private final PetProfileRepository profiles; private final SocialPostRepository posts; private final PetFollowRepository follows;
  private final FriendRequestRepository requests; private final PetReminderRepository reminders; private final PlayDateRepository playDates;
  private final MeetupRepository meetups; private final PetSitterRepository sitters; private final PetOrderRepository orders; private final PetMemoryRepository memories;

  public SocialController(PetProfileRepository profiles, SocialPostRepository posts, PetFollowRepository follows, FriendRequestRepository requests,
      PetReminderRepository reminders, PlayDateRepository playDates, MeetupRepository meetups, PetSitterRepository sitters,
      PetOrderRepository orders, PetMemoryRepository memories){this.profiles=profiles;this.posts=posts;this.follows=follows;this.requests=requests;this.reminders=reminders;this.playDates=playDates;this.meetups=meetups;this.sitters=sitters;this.orders=orders;this.memories=memories;}

  @GetMapping("/profiles") public List<PetProfile> profiles(){return profiles.findAll();}
  @PostMapping("/profiles") public ResponseEntity<?> createProfile(@RequestBody PetProfile p){if(p.getHandle()==null||p.getName()==null)return ResponseEntity.badRequest().body(Map.of("error","name and handle required")); return ResponseEntity.status(201).body(profiles.save(p));}
  @GetMapping("/profiles/{id}") public ResponseEntity<PetProfile> profile(@PathVariable Long id){return profiles.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());}

  @GetMapping("/posts") public List<SocialPost> posts(){return posts.findAll();}
  @GetMapping("/profiles/{id}/posts") public List<SocialPost> postsByPet(@PathVariable Long id){return posts.findByPetProfileIdOrderByCreatedAtDesc(id);}
  @PostMapping("/posts") public ResponseEntity<?> createPost(@RequestBody SocialPost p){if(!profiles.existsById(p.getPetProfileId()))return ResponseEntity.badRequest().body(Map.of("error","pet profile not found")); return ResponseEntity.status(201).body(posts.save(p));}
  @PostMapping("/posts/{id}/paw") public ResponseEntity<?> paw(@PathVariable Long id){return posts.findById(id).map(p->{p.setPawCount(p.getPawCount()+1);return ResponseEntity.ok(posts.save(p));}).orElse(ResponseEntity.notFound().build());}

  @PostMapping("/follows") public ResponseEntity<?> follow(@RequestBody PetFollow f){if(Objects.equals(f.getFollowerPetId(),f.getFollowingPetId()))return ResponseEntity.badRequest().body(Map.of("error","You cannot follow your own pet profile.")); if(!profiles.existsById(f.getFollowerPetId())||!profiles.existsById(f.getFollowingPetId()))return ResponseEntity.badRequest().body(Map.of("error","pet profile not found")); if(follows.existsByFollowerPetIdAndFollowingPetId(f.getFollowerPetId(),f.getFollowingPetId()))return ResponseEntity.status(409).body(Map.of("error","already following")); PetFollow saved=follows.save(f); profiles.findById(f.getFollowerPetId()).ifPresent(p->{p.setFollowing(p.getFollowing()+1);profiles.save(p);}); profiles.findById(f.getFollowingPetId()).ifPresent(p->{p.setFollowers(p.getFollowers()+1);profiles.save(p);}); return ResponseEntity.status(201).body(saved);}
  @GetMapping("/profiles/{id}/followers") public List<PetFollow> followers(@PathVariable Long id){return follows.findByFollowingPetId(id);}
  @GetMapping("/profiles/{id}/following") public List<PetFollow> following(@PathVariable Long id){return follows.findByFollowerPetId(id);}

  @PostMapping("/friend-requests") public ResponseEntity<?> request(@RequestBody FriendRequest r){if(Objects.equals(r.getFromPetId(),r.getToPetId()))return ResponseEntity.badRequest().body(Map.of("error","You cannot send a friend request to your own pet profile.")); if(!profiles.existsById(r.getFromPetId())||!profiles.existsById(r.getToPetId()))return ResponseEntity.badRequest().body(Map.of("error","pet profile not found")); return ResponseEntity.status(201).body(requests.save(r));}
  @GetMapping("/profiles/{id}/friend-requests") public List<FriendRequest> pending(@PathVariable Long id){return requests.findByToPetIdAndStatus(id,"PENDING");}
  @PatchMapping("/friend-requests/{id}/{status}") public ResponseEntity<?> updateRequest(@PathVariable Long id,@PathVariable String status){return requests.findById(id).map(r->{r.setStatus(status.toUpperCase());return ResponseEntity.ok(requests.save(r));}).orElse(ResponseEntity.notFound().build());}

  @GetMapping("/profiles/{id}/reminders") public List<PetReminder> reminders(@PathVariable Long id){return reminders.findByPetProfileIdOrderByDueDateAsc(id);}
  @PostMapping("/reminders") public ResponseEntity<PetReminder> reminder(@RequestBody PetReminder r){return ResponseEntity.status(201).body(reminders.save(r));}
  @PatchMapping("/reminders/{id}/complete") public ResponseEntity<?> complete(@PathVariable Long id){return reminders.findById(id).map(r->{r.setCompleted(true);return ResponseEntity.ok(reminders.save(r));}).orElse(ResponseEntity.notFound().build());}

  @PostMapping("/play-dates") public ResponseEntity<?> playDate(@RequestBody PlayDate p){if(Objects.equals(p.getHostPetId(),p.getGuestPetId()))return ResponseEntity.badRequest().body(Map.of("error","Choose another pet for the play date.")); return ResponseEntity.status(201).body(playDates.save(p));}
  @GetMapping("/profiles/{id}/play-dates") public List<PlayDate> playDates(@PathVariable Long id){return playDates.findByHostPetIdOrGuestPetIdOrderByScheduledAtAsc(id,id);}

  @GetMapping("/meetups") public List<Meetup> meetups(){return meetups.findAllByOrderByScheduledAtAsc();}
  @PostMapping("/meetups") public ResponseEntity<Meetup> meetup(@RequestBody Meetup m){return ResponseEntity.status(201).body(meetups.save(m));}
  @PostMapping("/meetups/{id}/join") public ResponseEntity<?> join(@PathVariable Long id){return meetups.findById(id).map(m->{m.setAttendeeCount(m.getAttendeeCount()+1);return ResponseEntity.ok(meetups.save(m));}).orElse(ResponseEntity.notFound().build());}

  @GetMapping("/sitters") public List<PetSitter> sitters(){return sitters.findByAvailableTrue();}
  @PostMapping("/sitters") public ResponseEntity<PetSitter> sitter(@RequestBody PetSitter s){return ResponseEntity.status(201).body(sitters.save(s));}

  @GetMapping("/profiles/{id}/orders") public List<PetOrder> orders(@PathVariable Long id){return orders.findByPetProfileIdOrderByCreatedAtDesc(id);}
  @PostMapping("/orders") public ResponseEntity<PetOrder> order(@RequestBody PetOrder o){return ResponseEntity.status(201).body(orders.save(o));}

  @GetMapping("/profiles/{id}/memories") public List<PetMemory> memories(@PathVariable Long id){return memories.findByPetProfileIdOrderByMemoryDateDesc(id);}
  @PostMapping("/memories") public ResponseEntity<PetMemory> memory(@RequestBody PetMemory m){return ResponseEntity.status(201).body(memories.save(m));}

  @GetMapping("/health") public Map<String,Object> health(){return Map.of("status","UP","modules",List.of("profiles","posts","follows","friends","reminders","playDates","meetups","sitters","orders","memories"));}
}
