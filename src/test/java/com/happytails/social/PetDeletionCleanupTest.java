package com.happytails.social;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PetDeletionCleanupTest {
  @Autowired MockMvc mvc;
  @Autowired ObjectMapper json;
  @Autowired SocialPostRepository posts;
  @Autowired PostPawRepository paws;
  @Autowired PostCommentRepository comments;

  @Test
  void deletingPetRemovesInteractionsAttachedToItsPosts() throws Exception {
    AccountPet owner=create("cleanup-owner@example.com","Cleanup Max","cleanup_max");
    AccountPet friend=create("cleanup-friend@example.com","Cleanup Luna","cleanup_luna");

    MvcResult created=mvc.perform(post("/api/social/posts").session(owner.session)
        .contentType("application/json").content("{\"caption\":\"Temporary post\"}"))
      .andExpect(status().isCreated()).andReturn();
    long postId=json.readTree(created.getResponse().getContentAsString()).get("id").asLong();

    mvc.perform(post("/api/interactions/posts/{id}/paw",postId).session(friend.session))
      .andExpect(status().isOk());
    mvc.perform(post("/api/interactions/posts/{id}/comments",postId).session(friend.session)
        .contentType("application/json").content("{\"body\":\"Nice post\"}"))
      .andExpect(status().isCreated());

    assertFalse(paws.findByPostId(postId).isEmpty());
    assertFalse(comments.findByPostId(postId).isEmpty());

    mvc.perform(delete("/api/auth/pets/{id}",owner.petId).session(owner.session)
        .contentType("application/json").content("{\"confirmName\":\"Cleanup Max\"}"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.deleted").value(true));

    assertFalse(posts.existsById(postId));
    assertTrue(paws.findByPostId(postId).isEmpty());
    assertTrue(comments.findByPostId(postId).isEmpty());
  }

  private AccountPet create(String email,String name,String handle) throws Exception {
    MvcResult signup=mvc.perform(post("/api/auth/signup").contentType("application/json")
        .content("{\"email\":\""+email+"\",\"password\":\"TestPass123!\"}"))
      .andExpect(status().isCreated()).andReturn();
    MockHttpSession session=(MockHttpSession)signup.getRequest().getSession(false);
    MvcResult pet=mvc.perform(post("/api/auth/pets").session(session).contentType("application/json")
        .content("{\"name\":\""+name+"\",\"handle\":\""+handle+"\",\"species\":\"Dog\"}"))
      .andExpect(status().isCreated()).andReturn();
    long petId=json.readTree(pet.getResponse().getContentAsString()).get("id").asLong();
    return new AccountPet(session,petId);
  }

  record AccountPet(MockHttpSession session,long petId){}
}
