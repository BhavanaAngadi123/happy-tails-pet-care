package com.happytails.social;

import jakarta.servlet.http.HttpSession;
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

  @GetMapping("/profiles") public List<Map<String,Object>> profiles(HttpSession session){Long viewer=activePet(session);return profiles.findAll().stream().map(p->publicProfile(p,viewer)).toList();}
  @PostMapping("/profiles") public ResponseEntity<?> createProfile(@RequestBody PetProfile p){if(p.getHandle()==null||p.getName()==null)return ResponseEntity.badRequest().body(Map.of("error","name and handle required")); return ResponseEntity.status(201).body(profiles.save(p));}
  @GetMapping("/profiles/{id}") public ResponseEntity<?> profile(@PathVariable Long id,HttpSession session){Long viewer=activePet(session);return profiles.findById(id).<ResponseEntity<?>>map(p->ResponseEntity.ok(publicProfile(p,viewer))).orElseGet(()->ResponseEntity.notFound().build());}

  @GetMapping("/posts") public List<SocialPost> posts(HttpSession session){Long viewer=activePet(session);return posts.findAll().stream().filter(p->canViewPosts(viewer,p.getPetProfileId())).toList();}
  @GetMapping("/profiles/{id}/posts") public List<SocialPost> postsByPet(@PathVariable Long id,HttpSession session){Long viewer=activePet(session);return canViewPosts(viewer,id)?posts.findByPetProfileIdOrderByCreatedAtDesc(id):List.of();}
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

  @PostMapping("/play-dates") public ResponseEntity<?> playDate(@RequestBody PlayDate p,HttpSession session){Long active=activePet(session);if(active==null)return ResponseEntity.status(401).body(Map.of("error","Please log in first."));if(p.getGuestPetId()==null)return ResponseEntity.badRequest().body(Map.of("error","Choose a valid pet."));Optional<PetProfile> guest=profiles.findById(p.getGuestPetId());if(guest.isEmpty())return ResponseEntity.badRequest().body(Map.of("error","Choose a valid pet."));if(Objects.equals(active,p.getGuestPetId()))return ResponseEntity.badRequest().body(Map.of("error","Choose another pet for the play date."));String permission=guest.get().getPlayDatePermission()==null?"EVERYONE":guest.get().getPlayDatePermission();if("NOBODY".equalsIgnoreCase(permission))return ResponseEntity.status(403).body(Map.of("error",guest.get().getName()+" is not accepting play-date requests."));if("FOLLOWING".equalsIgnoreCase(permission)&&!follows.existsByFollowerPetIdAndFollowingPetId(guest.get().getId(),active))return ResponseEntity.status(403).body(Map.of("error","Only pets this account follows can send play-date requests."));p.setHostPetId(active);p.setStatus("PENDING");return ResponseEntity.status(201).body(playDates.save(p));}
  @GetMapping("/profiles/{id}/play-dates") public List<PlayDate> playDates(@PathVariable Long id){return playDates.findByHostPetIdOrGuestPetIdOrderByScheduledAtAsc(id,id);}
  @PatchMapping("/play-dates/{id}/{status}") public ResponseEntity<?> updatePlayDate(@PathVariable Long id,@PathVariable String status,HttpSession session){Long active=activePet(session);if(active==null)return ResponseEntity.status(401).body(Map.of("error","Please log in first."));String next=status.toUpperCase();if(!Set.of("ACCEPTED","DECLINED","CANCELLED").contains(next))return ResponseEntity.badRequest().body(Map.of("error","Invalid play date status."));return playDates.findById(id).<ResponseEntity<?>>map(p->{boolean guest=Objects.equals(active,p.getGuestPetId()),host=Objects.equals(active,p.getHostPetId());if(next.equals("CANCELLED")&&!host)return ResponseEntity.status(403).body(Map.of("error","Only the pet who created this play date can cancel it."));if((next.equals("ACCEPTED")||next.equals("DECLINED"))&&!guest)return ResponseEntity.status(403).body(Map.of("error","Only the invited pet can respond."));p.setStatus(next);return ResponseEntity.ok(playDates.save(p));}).orElseGet(()->ResponseEntity.notFound().build());}

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
  private Long activePet(HttpSession session){Object x=session.getAttribute("activePetId");return x instanceof Long?(Long)x:null;}
  private boolean areFriends(Long a,Long b){if(a==null||b==null)return false;return requests.existsByFromPetIdAndToPetIdAndStatus(a,b,"ACCEPTED")||requests.existsByFromPetIdAndToPetIdAndStatus(b,a,"ACCEPTED");}
  private boolean canViewPosts(Long viewer,Long owner){if(Objects.equals(viewer,owner))return true;Optional<PetProfile> p=profiles.findById(owner);return p.isPresent()&&(!p.get().isPrivateAccount()||areFriends(viewer,owner));}
  private Map<String,Object> publicProfile(PetProfile p,Long viewer){boolean self=Objects.equals(viewer,p.getId()),friend=areFriends(viewer,p.getId());Map<String,Object> m=new LinkedHashMap<>();m.put("id",p.getId());m.put("handle",p.getHandle());m.put("name",p.getName());m.put("species",p.getSpecies());m.put("breed",p.getBreed());m.put("bio",p.getBio());m.put("avatarUrl",p.getAvatarUrl());m.put("location",self||p.isShowLocation()?p.getLocation():"Location private");m.put("birthday",p.getBirthday());m.put("adoptionDate",self?p.getAdoptionDate():null);m.put("age",p.getAge());m.put("gender",p.getGender());m.put("personality",p.getPersonality());m.put("favoriteActivities",p.getFavoriteActivities());m.put("petPreferences",self?p.getPetPreferences():null);m.put("privateAccount",p.isPrivateAccount());m.put("showLocation",self?p.isShowLocation():false);m.put("messagePermission",self?p.getMessagePermission():null);m.put("playDatePermission",self?p.getPlayDatePermission():null);m.put("followers",p.getFollowers());m.put("following",p.getFollowing());m.put("contentVisible",!p.isPrivateAccount()||self||friend);return m;}
}
