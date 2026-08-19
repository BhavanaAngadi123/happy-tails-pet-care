package com.happytails.social;

import jakarta.persistence.*;
import java.time.*;

@MappedSuperclass
abstract class BaseEntity {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
  public Long getId(){return id;}
}

@Entity @Table(name="pet_profiles")
class PetProfile extends BaseEntity {
  @Column(nullable=false,unique=true) String handle;
  @Column(nullable=false) String name;
  String species; String breed; String bio;
  @Column(length=1000000) String avatarUrl;
  String location;
  LocalDate birthday;
  Integer age;
  String gender;
  String personality;
  String favoriteActivities;
  String petPreferences;
  int followers; int following;
  public PetProfile(){}
  public String getHandle(){return handle;} public void setHandle(String v){handle=v;}
  public String getName(){return name;} public void setName(String v){name=v;}
  public String getSpecies(){return species;} public void setSpecies(String v){species=v;}
  public String getBreed(){return breed;} public void setBreed(String v){breed=v;}
  public String getBio(){return bio;} public void setBio(String v){bio=v;}
  public String getAvatarUrl(){return avatarUrl;} public void setAvatarUrl(String v){avatarUrl=v;}
  public String getLocation(){return location;} public void setLocation(String v){location=v;}
  public LocalDate getBirthday(){return birthday;} public void setBirthday(LocalDate v){birthday=v;}
  public Integer getAge(){return age;} public void setAge(Integer v){age=v;}
  public String getGender(){return gender;} public void setGender(String v){gender=v;}
  public String getPersonality(){return personality;} public void setPersonality(String v){personality=v;}
  public String getFavoriteActivities(){return favoriteActivities;} public void setFavoriteActivities(String v){favoriteActivities=v;}
  public String getPetPreferences(){return petPreferences;} public void setPetPreferences(String v){petPreferences=v;}
  public int getFollowers(){return followers;} public void setFollowers(int v){followers=v;}
  public int getFollowing(){return following;} public void setFollowing(int v){following=v;}
}

@Entity @Table(name="social_posts")
class SocialPost extends BaseEntity {
  @Column(nullable=false) Long petProfileId;
  @Column(nullable=false,length=2000) String caption;
  String mediaUrl; int pawCount; int commentCount; LocalDateTime createdAt=LocalDateTime.now();
  public SocialPost(){}
  public Long getPetProfileId(){return petProfileId;} public void setPetProfileId(Long v){petProfileId=v;}
  public String getCaption(){return caption;} public void setCaption(String v){caption=v;}
  public String getMediaUrl(){return mediaUrl;} public void setMediaUrl(String v){mediaUrl=v;}
  public int getPawCount(){return pawCount;} public void setPawCount(int v){pawCount=v;}
  public int getCommentCount(){return commentCount;} public void setCommentCount(int v){commentCount=v;}
  public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;}
}

@Entity @Table(name="pet_follows", uniqueConstraints=@UniqueConstraint(columnNames={"followerPetId","followingPetId"}))
class PetFollow extends BaseEntity {
  Long followerPetId; Long followingPetId; LocalDateTime createdAt=LocalDateTime.now();
  public PetFollow(){}
  public Long getFollowerPetId(){return followerPetId;} public void setFollowerPetId(Long v){followerPetId=v;}
  public Long getFollowingPetId(){return followingPetId;} public void setFollowingPetId(Long v){followingPetId=v;}
  public LocalDateTime getCreatedAt(){return createdAt;}
}

@Entity @Table(name="friend_requests")
class FriendRequest extends BaseEntity {
  Long fromPetId; Long toPetId; String status="PENDING"; LocalDateTime createdAt=LocalDateTime.now();
  public FriendRequest(){}
  public Long getFromPetId(){return fromPetId;} public void setFromPetId(Long v){fromPetId=v;}
  public Long getToPetId(){return toPetId;} public void setToPetId(Long v){toPetId=v;}
  public String getStatus(){return status;} public void setStatus(String v){status=v;}
  public LocalDateTime getCreatedAt(){return createdAt;}
}

@Entity @Table(name="pet_reminders")
class PetReminder extends BaseEntity {
  Long petProfileId; String type; String title; LocalDate dueDate; boolean completed;
  public PetReminder(){}
  public Long getPetProfileId(){return petProfileId;} public void setPetProfileId(Long v){petProfileId=v;}
  public String getType(){return type;} public void setType(String v){type=v;}
  public String getTitle(){return title;} public void setTitle(String v){title=v;}
  public LocalDate getDueDate(){return dueDate;} public void setDueDate(LocalDate v){dueDate=v;}
  public boolean isCompleted(){return completed;} public void setCompleted(boolean v){completed=v;}
}

@Entity @Table(name="play_dates")
class PlayDate extends BaseEntity {
  Long hostPetId; Long guestPetId; String location; LocalDateTime scheduledAt; String status="PENDING";
  public PlayDate(){}
  public Long getHostPetId(){return hostPetId;} public void setHostPetId(Long v){hostPetId=v;}
  public Long getGuestPetId(){return guestPetId;} public void setGuestPetId(Long v){guestPetId=v;}
  public String getLocation(){return location;} public void setLocation(String v){location=v;}
  public LocalDateTime getScheduledAt(){return scheduledAt;} public void setScheduledAt(LocalDateTime v){scheduledAt=v;}
  public String getStatus(){return status;} public void setStatus(String v){status=v;}
}

@Entity @Table(name="pet_meetups")
class Meetup extends BaseEntity {
  String title; String description; String location; LocalDateTime scheduledAt; int attendeeCount;
  public Meetup(){}
  public String getTitle(){return title;} public void setTitle(String v){title=v;}
  public String getDescription(){return description;} public void setDescription(String v){description=v;}
  public String getLocation(){return location;} public void setLocation(String v){location=v;}
  public LocalDateTime getScheduledAt(){return scheduledAt;} public void setScheduledAt(LocalDateTime v){scheduledAt=v;}
  public int getAttendeeCount(){return attendeeCount;} public void setAttendeeCount(int v){attendeeCount=v;}
}

@Entity @Table(name="pet_sitters")
class PetSitter extends BaseEntity {
  String name; String city; String specialties; double rating; int reviewCount; boolean available=true;
  public PetSitter(){}
  public String getName(){return name;} public void setName(String v){name=v;}
  public String getCity(){return city;} public void setCity(String v){city=v;}
  public String getSpecialties(){return specialties;} public void setSpecialties(String v){specialties=v;}
  public double getRating(){return rating;} public void setRating(double v){rating=v;}
  public int getReviewCount(){return reviewCount;} public void setReviewCount(int v){reviewCount=v;}
  public boolean isAvailable(){return available;} public void setAvailable(boolean v){available=v;}
}

@Entity @Table(name="pet_orders")
class PetOrder extends BaseEntity {
  Long petProfileId; String itemName; int quantity; double totalAmount; String status="PLACED"; LocalDateTime createdAt=LocalDateTime.now();
  public PetOrder(){}
  public Long getPetProfileId(){return petProfileId;} public void setPetProfileId(Long v){petProfileId=v;}
  public String getItemName(){return itemName;} public void setItemName(String v){itemName=v;}
  public int getQuantity(){return quantity;} public void setQuantity(int v){quantity=v;}
  public double getTotalAmount(){return totalAmount;} public void setTotalAmount(double v){totalAmount=v;}
  public String getStatus(){return status;} public void setStatus(String v){status=v;}
  public LocalDateTime getCreatedAt(){return createdAt;}
}

@Entity @Table(name="pet_memories")
class PetMemory extends BaseEntity {
  Long petProfileId; String title; String mediaUrl; LocalDate memoryDate; String description;
  public PetMemory(){}
  public Long getPetProfileId(){return petProfileId;} public void setPetProfileId(Long v){petProfileId=v;}
  public String getTitle(){return title;} public void setTitle(String v){title=v;}
  public String getMediaUrl(){return mediaUrl;} public void setMediaUrl(String v){mediaUrl=v;}
  public LocalDate getMemoryDate(){return memoryDate;} public void setMemoryDate(LocalDate v){memoryDate=v;}
  public String getDescription(){return description;} public void setDescription(String v){description=v;}
}
