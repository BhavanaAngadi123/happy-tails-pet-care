package com.happytails.social;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class HappyTailsJourneySmokeTest {
  @Autowired MockMvc mvc;
  @Autowired ObjectMapper json;

  @Test
  void corePetJourneyWorksAndIdentityCannotBeSpoofed() throws Exception {
    AccountPet max=createAccountAndPet("qa-max@example.com","Max","qa_max","Dog","Golden Retriever","Boston, MA");
    AccountPet luna=createAccountAndPet("qa-luna@example.com","Luna","qa_luna","Dog","Golden Retriever","Boston, MA");

    mvc.perform(post("/api/social/posts").session(max.session)
        .contentType("application/json")
        .content("{\"petProfileId\":"+luna.petId+",\"caption\":\"First Happy Tails memory\"}"))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.petProfileId").value(max.petId));

    mvc.perform(post("/api/social/follows").session(max.session)
        .contentType("application/json")
        .content("{\"followerPetId\":"+luna.petId+",\"followingPetId\":"+luna.petId+"}"))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.followerPetId").value(max.petId))
      .andExpect(jsonPath("$.followingPetId").value(luna.petId));

    MvcResult request=mvc.perform(post("/api/social/friend-requests").session(max.session)
        .contentType("application/json")
        .content("{\"fromPetId\":"+luna.petId+",\"toPetId\":"+luna.petId+"}"))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.fromPetId").value(max.petId))
      .andReturn();
    long requestId=json.readTree(request.getResponse().getContentAsString()).get("id").asLong();

    mvc.perform(patch("/api/social/friend-requests/{id}/ACCEPTED",requestId).session(max.session))
      .andExpect(status().isForbidden());
    mvc.perform(patch("/api/social/friend-requests/{id}/ACCEPTED",requestId).session(luna.session))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value("ACCEPTED"));

    mvc.perform(post("/api/messages/with/{petId}",luna.petId).session(max.session)
        .contentType("application/json").content("{\"body\":\"Want to meet at the park?\"}"))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.fromPetId").value(max.petId))
      .andExpect(jsonPath("$.toPetId").value(luna.petId));

    mvc.perform(get("/api/messages/with/{petId}",max.petId).session(luna.session))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$[0].body").value("Want to meet at the park?"));

    mvc.perform(post("/api/social/play-dates").session(max.session)
        .contentType("application/json")
        .content("{\"guestPetId\":"+luna.petId+",\"location\":\"Boston Common\",\"scheduledAt\":\"2030-08-24T10:00:00\"}"))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.hostPetId").value(max.petId))
      .andExpect(jsonPath("$.guestPetId").value(luna.petId))
      .andExpect(jsonPath("$.location").value("Boston Common"))
      .andExpect(jsonPath("$.status").value("PENDING"));

    mvc.perform(get("/api/social/profiles/{id}/reminders",luna.petId).session(max.session))
      .andExpect(status().isForbidden());

    mvc.perform(post("/api/social/memories").session(max.session)
        .contentType("application/json")
        .content("{\"petProfileId\":"+luna.petId+",\"title\":\"Gotcha Day\",\"memoryDate\":\"2026-08-19\",\"description\":\"A favorite day\"}"))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.petProfileId").value(max.petId));
  }

  @Test
  void pawsToggleOncePerPetAndLegacyRouteUsesSameState() throws Exception {
    AccountPet max=createAccountAndPet("qa-paw-max@example.com","Max Paw","qa_paw_max","Dog","Retriever","Boston, MA");
    MvcResult created=mvc.perform(post("/api/social/posts").session(max.session)
        .contentType("application/json").content("{\"caption\":\"Paw integrity\"}"))
      .andExpect(status().isCreated()).andReturn();
    long postId=json.readTree(created.getResponse().getContentAsString()).get("id").asLong();

    mvc.perform(post("/api/social/posts/{id}/paw",postId).session(max.session))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.pawed").value(true))
      .andExpect(jsonPath("$.pawCount").value(1));

    mvc.perform(post("/api/interactions/posts/{id}/paw",postId).session(max.session))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.pawed").value(false))
      .andExpect(jsonPath("$.pawCount").value(0));
  }

  @Test
  void shopIgnoresClientPriceAndUsesServerCatalog() throws Exception {
    AccountPet max=createAccountAndPet("qa-shop@example.com","Shop Max","qa_shop_max","Dog","Retriever","Boston, MA");

    mvc.perform(post("/api/social/orders").session(max.session)
        .contentType("application/json")
        .content("{\"productId\":\"toy-rope\",\"quantity\":2,\"totalAmount\":0.01,\"itemName\":\"Forged Item\"}"))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.itemName").value("Adventure Rope Toy"))
      .andExpect(jsonPath("$.quantity").value(2))
      .andExpect(jsonPath("$.totalAmount").value(29.98));

    mvc.perform(post("/api/social/orders").session(max.session)
        .contentType("application/json")
        .content("{\"productId\":\"not-real\",\"quantity\":1}"))
      .andExpect(status().isBadRequest());
  }

  @Test
  void unauthenticatedPrivateActionsAreRejected() throws Exception {
    mvc.perform(post("/api/social/posts").contentType("application/json").content("{\"caption\":\"No session\"}"))
      .andExpect(status().isUnauthorized());
    mvc.perform(get("/api/messages/inbox")).andExpect(status().isUnauthorized());
  }

  private AccountPet createAccountAndPet(String email,String name,String handle,String species,String breed,String location) throws Exception {
    MvcResult signup=mvc.perform(post("/api/auth/signup").contentType("application/json")
        .content("{\"email\":\""+email+"\",\"password\":\"TestPass123!\",\"displayName\":\"QA Parent\"}"))
      .andExpect(status().isCreated()).andReturn();
    MockHttpSession session=(MockHttpSession)signup.getRequest().getSession(false);

    MvcResult pet=mvc.perform(post("/api/auth/pets").session(session).contentType("application/json")
        .content("{\"name\":\""+name+"\",\"handle\":\""+handle+"\",\"species\":\""+species+"\",\"breed\":\""+breed+"\",\"location\":\""+location+"\",\"birthday\":\"2023-05-10\"}"))
      .andExpect(status().isCreated()).andReturn();
    JsonNode body=json.readTree(pet.getResponse().getContentAsString());
    return new AccountPet(session,body.get("id").asLong());
  }

  record AccountPet(MockHttpSession session,long petId){}
}
