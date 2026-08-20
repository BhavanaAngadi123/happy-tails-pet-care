package com.happytails.social;

import jakarta.persistence.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Entity
@Table(name="owner_credential_versions",uniqueConstraints=@UniqueConstraint(columnNames="ownerId"))
class OwnerCredentialVersion {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
 @Column(nullable=false) Long ownerId;
 @Column(nullable=false) long version=0;
}

interface OwnerCredentialVersionRepository extends JpaRepository<OwnerCredentialVersion,Long>{
 java.util.Optional<OwnerCredentialVersion> findByOwnerId(Long ownerId);
}

@Service
public class CredentialVersionService {
 private final OwnerCredentialVersionRepository repo;
 public CredentialVersionService(OwnerCredentialVersionRepository repo){this.repo=repo;}

 @Transactional
 public long current(Long ownerId){
  return repo.findByOwnerId(ownerId).map(x->x.version).orElseGet(()->{
   OwnerCredentialVersion x=new OwnerCredentialVersion();x.ownerId=ownerId;x.version=0;return repo.save(x).version;
  });
 }

 @Transactional
 public long rotate(Long ownerId){
  OwnerCredentialVersion x=repo.findByOwnerId(ownerId).orElseGet(()->{OwnerCredentialVersion n=new OwnerCredentialVersion();n.ownerId=ownerId;n.version=0;return n;});
  x.version++;
  return repo.save(x).version;
 }
}
