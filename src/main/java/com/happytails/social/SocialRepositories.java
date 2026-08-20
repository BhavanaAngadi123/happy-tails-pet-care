package com.happytails.social;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

interface PetProfileRepository extends JpaRepository<PetProfile,Long>{ Optional<PetProfile> findByHandle(String handle); }
interface SocialPostRepository extends JpaRepository<SocialPost,Long>{ List<SocialPost> findByPetProfileIdOrderByCreatedAtDesc(Long petProfileId); }
interface PetFollowRepository extends JpaRepository<PetFollow,Long>{ List<PetFollow> findByFollowerPetId(Long id); List<PetFollow> findByFollowingPetId(Long id); boolean existsByFollowerPetIdAndFollowingPetId(Long a,Long b); Optional<PetFollow> findByFollowerPetIdAndFollowingPetId(Long a,Long b); }
interface FriendRequestRepository extends JpaRepository<FriendRequest,Long>{ List<FriendRequest> findByToPetIdAndStatus(Long id,String status); boolean existsByFromPetIdAndToPetIdAndStatus(Long from,Long to,String status); List<FriendRequest> findByFromPetIdAndToPetIdOrFromPetIdAndToPetId(Long a,Long b,Long c,Long d); }
interface PetReminderRepository extends JpaRepository<PetReminder,Long>{ List<PetReminder> findByPetProfileIdOrderByDueDateAsc(Long id); }
interface PlayDateRepository extends JpaRepository<PlayDate,Long>{ List<PlayDate> findByHostPetIdOrGuestPetIdOrderByScheduledAtAsc(Long a,Long b); }
interface MeetupRepository extends JpaRepository<Meetup,Long>{ List<Meetup> findAllByOrderByScheduledAtAsc(); }
interface PetSitterRepository extends JpaRepository<PetSitter,Long>{ List<PetSitter> findByAvailableTrue(); }
interface PetOrderRepository extends JpaRepository<PetOrder,Long>{ List<PetOrder> findByPetProfileIdOrderByCreatedAtDesc(Long id); }
interface PetMemoryRepository extends JpaRepository<PetMemory,Long>{ List<PetMemory> findByPetProfileIdOrderByMemoryDateDesc(Long id); }
