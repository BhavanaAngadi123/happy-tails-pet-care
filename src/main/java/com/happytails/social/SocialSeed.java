package com.happytails.social;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import java.time.*;

@Component
class SocialSeed {
  private final PetProfileRepository profiles; private final SocialPostRepository posts; private final PetReminderRepository reminders;
  private final MeetupRepository meetups; private final PetSitterRepository sitters; private final PetMemoryRepository memories;
  SocialSeed(PetProfileRepository profiles,SocialPostRepository posts,PetReminderRepository reminders,MeetupRepository meetups,PetSitterRepository sitters,PetMemoryRepository memories){this.profiles=profiles;this.posts=posts;this.reminders=reminders;this.meetups=meetups;this.sitters=sitters;this.memories=memories;}

  @EventListener(ApplicationReadyEvent.class)
  public void seedAfterStartup(){
    Thread t=new Thread(this::seedSafely,"happy-tails-demo-seed");
    t.setDaemon(true);
    t.start();
  }

  private void seedSafely(){
    try { seed(); } catch (Exception ex) { System.err.println("Demo seed skipped: "+ex.getMessage()); }
  }

  private void seed(){
    if(profiles.count()>0)return;
    PetProfile buddy=pet("@buddy_thegolden","Buddy","Dog","Golden Retriever","Fetch expert. Treat enthusiast. Making new pet friends!","Bengaluru",1200,320);
    pet("@coco_poodle","Coco","Dog","Poodle","Playful, curly and always ready for a walk.","Bengaluru",834,181);
    pet("@simba_cat","Simba","Cat","Domestic Shorthair","Window watcher and professional napper.","Bengaluru",641,204);
    pet("@pepper_bunny","Pepper","Rabbit","Mini Lop","Carrots, tunnels and tiny adventures.","Bengaluru",505,162);
    pet("@kiwi_parrot","Kiwi","Bird","Parrot","Talkative explorer with colorful opinions.","Bengaluru",923,270);
    pet("@shellby","Shellby","Turtle","Red-eared Slider","Slow travels, sunny rocks, good vibes.","Bengaluru",388,91);
    SocialPost post=new SocialPost();post.setPetProfileId(buddy.getId());post.setCaption("Sunshine, zoomies and Saturday! ☀️🐾 #GoldenHour #HappyTails #WeekendVibes");post.setPawCount(128);post.setCommentCount(24);posts.save(post);
    reminder(buddy.getId(),"VACCINATION","Annual vaccination",LocalDate.of(2026,8,25)); reminder(buddy.getId(),"VET","Vet checkup",LocalDate.of(2026,9,2)); reminder(buddy.getId(),"DEWORMING","Deworming",LocalDate.of(2026,9,15));
    Meetup m=new Meetup();m.setTitle("Pet Social Meetup");m.setDescription("All friendly pets welcome");m.setLocation("Cubbon Park");m.setScheduledAt(LocalDateTime.of(2026,8,30,11,0));m.setAttendeeCount(18);meetups.save(m);
    sitter("Pawfect Care","Bengaluru","Dogs, cats, rabbits",4.9,128);sitter("Happy Paws Sitting","Bengaluru","Birds, fish, reptiles, small pets",5.0,203);
    PetMemory mem=new PetMemory();mem.setPetProfileId(buddy.getId());mem.setTitle("Buddy's Birthday");mem.setMemoryDate(LocalDate.of(2025,8,10));mem.setDescription("Birthday treats and park party");memories.save(mem);
  }
  private PetProfile pet(String handle,String name,String species,String breed,String bio,String location,int followers,int following){PetProfile p=new PetProfile();p.setHandle(handle);p.setName(name);p.setSpecies(species);p.setBreed(breed);p.setBio(bio);p.setLocation(location);p.setFollowers(followers);p.setFollowing(following);return profiles.save(p);}
  private void reminder(Long id,String type,String title,LocalDate due){PetReminder r=new PetReminder();r.setPetProfileId(id);r.setType(type);r.setTitle(title);r.setDueDate(due);reminders.save(r);}
  private void sitter(String name,String city,String specialties,double rating,int reviews){PetSitter s=new PetSitter();s.setName(name);s.setCity(city);s.setSpecialties(specialties);s.setRating(rating);s.setReviewCount(reviews);sitters.save(s);}
}
