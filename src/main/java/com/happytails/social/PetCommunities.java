package com.happytails.social;

import jakarta.persistence.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity @Table(name="pet_communities")
class PetCommunity extends BaseEntity {
  @Column(nullable=false) String name;
  @Column(nullable=false) String type;
  String species; String breed; String location; String description;
  int memberCount;
  public String getName(){return name;} public void setName(String v){name=v;}
  public String getType(){return type;} public void setType(String v){type=v;}
  public String getSpecies(){return species;} public void setSpecies(String v){species=v;}
  public String getBreed(){return breed;} public void setBreed(String v){breed=v;}
  public String getLocation(){return location;} public void setLocation(String v){location=v;}
  public String getDescription(){return description;} public void setDescription(String v){description=v;}
  public int getMemberCount(){return memberCount;} public void setMemberCount(int v){memberCount=v;}
}

@Entity @Table(name="pet_community_members", uniqueConstraints=@UniqueConstraint(columnNames={"communityId","petProfileId"}))
class PetCommunityMember extends BaseEntity {
  Long communityId; Long petProfileId; LocalDateTime joinedAt=LocalDateTime.now();
  public Long getCommunityId(){return communityId;} public void setCommunityId(Long v){communityId=v;}
  public Long getPetProfileId(){return petProfileId;} public void setPetProfileId(Long v){petProfileId=v;}
  public LocalDateTime getJoinedAt(){return joinedAt;}
}

@Entity @Table(name="pet_community_posts")
class PetCommunityPost extends BaseEntity {
  Long communityId; Long petProfileId;
  @Column(nullable=false,length=2000) String body;
  LocalDateTime createdAt=LocalDateTime.now();
  public Long getCommunityId(){return communityId;} public void setCommunityId(Long v){communityId=v;}
  public Long getPetProfileId(){return petProfileId;} public void setPetProfileId(Long v){petProfileId=v;}
  public String getBody(){return body;} public void setBody(String v){body=v;}
  public LocalDateTime getCreatedAt(){return createdAt;}
}

interface PetCommunityRepository extends JpaRepository<PetCommunity,Long>{List<PetCommunity> findAllByOrderByMemberCountDesc();}
interface PetCommunityMemberRepository extends JpaRepository<PetCommunityMember,Long>{boolean existsByCommunityIdAndPetProfileId(Long c,Long p);List<PetCommunityMember> findByPetProfileId(Long p);Optional<PetCommunityMember> findByCommunityIdAndPetProfileId(Long c,Long p);}
interface PetCommunityPostRepository extends JpaRepository<PetCommunityPost,Long>{List<PetCommunityPost> findByCommunityIdOrderByCreatedAtDesc(Long id);}

@RestController @RequestMapping("/api/communities")
class PetCommunityController {
  private final PetCommunityRepository communities; private final PetCommunityMemberRepository members; private final PetCommunityPostRepository posts; private final PetProfileRepository profiles;
  PetCommunityController(PetCommunityRepository communities,PetCommunityMemberRepository members,PetCommunityPostRepository posts,PetProfileRepository profiles){this.communities=communities;this.members=members;this.posts=posts;this.profiles=profiles;}
  private Long active(HttpSession s){Object x=s.getAttribute("activePetId");return x instanceof Long?(Long)x:null;}
  private void ensureDefaults(PetProfile p){if(communities.count()>0)return;List<PetCommunity> seed=new ArrayList<>();seed.add(group((p.getSpecies()==null?"Pet":p.getSpecies())+" Community","SPECIES",p.getSpecies(),null,null,"Connect with pets of the same species."));if(p.getBreed()!=null&&!p.getBreed().isBlank())seed.add(group(p.getBreed()+" Club","BREED",p.getSpecies(),p.getBreed(),null,"Breed-specific stories, tips, playmates and meetups."));if(p.getLocation()!=null&&!p.getLocation().isBlank())seed.add(group("Pets of "+p.getLocation(),"LOCAL",null,null,p.getLocation(),"Local pets, nearby play dates and community events."));seed.add(group("All Pets Welcome","GENERAL",null,null,null,"A friendly mixed-pet community for every companion animal."));communities.saveAll(seed);}
  private PetCommunity group(String name,String type,String species,String breed,String location,String description){PetCommunity c=new PetCommunity();c.setName(name);c.setType(type);c.setSpecies(species);c.setBreed(breed);c.setLocation(location);c.setDescription(description);return c;}
  @GetMapping ResponseEntity<?> list(HttpSession s){Long me=active(s);if(me==null)return ResponseEntity.status(401).body(Map.of("error","login required"));PetProfile p=profiles.findById(me).orElse(null);if(p==null)return ResponseEntity.notFound().build();ensureDefaults(p);List<Long> joined=members.findByPetProfileId(me).stream().map(PetCommunityMember::getCommunityId).toList();List<Map<String,Object>> out=new ArrayList<>();for(PetCommunity c:communities.findAllByOrderByMemberCountDesc()){Map<String,Object> m=new LinkedHashMap<>();m.put("id",c.getId());m.put("name",c.getName());m.put("type",c.getType());m.put("species",c.getSpecies());m.put("breed",c.getBreed());m.put("location",c.getLocation());m.put("description",c.getDescription());m.put("memberCount",c.getMemberCount());m.put("joined",joined.contains(c.getId()));out.add(m);}return ResponseEntity.ok(out);}
  @PostMapping("/{id}/join") ResponseEntity<?> join(@PathVariable Long id,HttpSession s){Long me=active(s);if(me==null)return ResponseEntity.status(401).body(Map.of("error","login required"));if(members.existsByCommunityIdAndPetProfileId(id,me))return ResponseEntity.ok(Map.of("joined",true));return communities.findById(id).<ResponseEntity<?>>map(c->{PetCommunityMember m=new PetCommunityMember();m.setCommunityId(id);m.setPetProfileId(me);members.save(m);c.setMemberCount(c.getMemberCount()+1);communities.save(c);return ResponseEntity.ok(Map.of("joined",true));}).orElseGet(()->ResponseEntity.notFound().build());}
  @DeleteMapping("/{id}/join") ResponseEntity<?> leave(@PathVariable Long id,HttpSession s){Long me=active(s);if(me==null)return ResponseEntity.status(401).body(Map.of("error","login required"));Optional<PetCommunityMember> member=members.findByCommunityIdAndPetProfileId(id,me);if(member.isEmpty())return ResponseEntity.ok(Map.of("joined",false));members.delete(member.get());communities.findById(id).ifPresent(c->{c.setMemberCount(Math.max(0,c.getMemberCount()-1));communities.save(c);});return ResponseEntity.ok(Map.of("joined",false));}
  @GetMapping("/{id}/posts") ResponseEntity<?> communityPosts(@PathVariable Long id,HttpSession s){if(active(s)==null)return ResponseEntity.status(401).body(Map.of("error","login required"));return ResponseEntity.ok(posts.findByCommunityIdOrderByCreatedAtDesc(id));}
  @PostMapping("/{id}/posts") ResponseEntity<?> createPost(@PathVariable Long id,@RequestBody Map<String,String> req,HttpSession s){Long me=active(s);if(me==null)return ResponseEntity.status(401).body(Map.of("error","login required"));if(!members.existsByCommunityIdAndPetProfileId(id,me))return ResponseEntity.status(403).body(Map.of("error","Join this community before posting."));String body=req.getOrDefault("body","").trim();if(body.isEmpty())return ResponseEntity.badRequest().body(Map.of("error","Post cannot be empty."));PetCommunityPost p=new PetCommunityPost();p.setCommunityId(id);p.setPetProfileId(me);p.setBody(body);return ResponseEntity.status(201).body(posts.save(p));}
}
