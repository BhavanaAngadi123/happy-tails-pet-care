package com.happytails.social;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Service
class OwnerAccountDeletionService {
 private final OwnerAccountRepository owners;
 private final OwnerPetLinkRepository links;
 private final PetAccountCleanupService petCleanup;
 @PersistenceContext EntityManager em;

 OwnerAccountDeletionService(OwnerAccountRepository owners,OwnerPetLinkRepository links,PetAccountCleanupService petCleanup){
  this.owners=owners;this.links=links;this.petCleanup=petCleanup;
 }

 @Transactional
 public void deleteOwner(Long ownerId){
  List<Long> petIds=links.findByOwnerId(ownerId).stream().map(OwnerPetLink::getPetProfileId).toList();
  for(Long petId:petIds)petCleanup.deletePet(petId);
  em.createNativeQuery("delete from password_reset_tokens where owner_id=:id").setParameter("id",ownerId).executeUpdate();
  em.createNativeQuery("delete from owner_credential_versions where owner_id=:id").setParameter("id",ownerId).executeUpdate();
  em.createNativeQuery("delete from owner_pet_links where owner_id=:id").setParameter("id",ownerId).executeUpdate();
  owners.deleteById(ownerId);
  em.flush();
 }
}

@RestController
@RequestMapping("/api/auth")
public class OwnerAccountDeletionController {
 private static final String CONFIRM_PHRASE="DELETE MY ACCOUNT";
 private final OwnerAccountRepository owners;
 private final OwnerAccountDeletionService deletion;
 private final BCryptPasswordEncoder encoder=new BCryptPasswordEncoder(10);

 public OwnerAccountDeletionController(OwnerAccountRepository owners,OwnerAccountDeletionService deletion){this.owners=owners;this.deletion=deletion;}

 @DeleteMapping("/account")
 public ResponseEntity<?> deleteAccount(@RequestBody(required=false) Map<String,String> body,HttpServletRequest request){
  HttpSession session=request.getSession(false);
  Long ownerId=ownerId(session);
  if(ownerId==null)return ResponseEntity.status(401).body(Map.of("error","Please log in first."));
  OwnerAccount owner=owners.findById(ownerId).orElse(null);
  if(owner==null){if(session!=null)session.invalidate();return ResponseEntity.status(401).body(Map.of("error","Please log in first."));}
  String password=body==null?"":body.getOrDefault("password","");
  String phrase=body==null?"":body.getOrDefault("confirmPhrase","").trim();
  if(!encoder.matches(password,owner.getPasswordHash()))return ResponseEntity.status(401).body(Map.of("error","Current password is incorrect."));
  if(!CONFIRM_PHRASE.equals(phrase))return ResponseEntity.badRequest().body(Map.of("error","Type DELETE MY ACCOUNT exactly to confirm permanent deletion."));
  deletion.deleteOwner(ownerId);
  session.invalidate();
  return ResponseEntity.ok(Map.of("deleted",true,"message","Your Happy Tails account and pet data were permanently deleted."));
 }

 private Long ownerId(HttpSession s){if(s==null)return null;Object x=s.getAttribute("ownerId");return x instanceof Long?(Long)x:null;}
}
