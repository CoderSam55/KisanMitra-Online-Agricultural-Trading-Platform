package com.kisanmitra.review;

import com.kisanmitra.offer.Offer;
import com.kisanmitra.offer.OfferRepository;
import com.kisanmitra.user.User;
import com.kisanmitra.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@RestController @RequestMapping("/api/reviews") @RequiredArgsConstructor
public class ReviewController {
  private final ReviewRepository reviews; private final UserRepository users; private final OfferRepository offers;
  @PostMapping public ResponseEntity<?> create(@RequestBody Map<String,Object> body, Authentication authentication) {
    User reviewer = user(authentication);
    Offer offer = offers.findById(UUID.fromString(String.valueOf(body.get("offerId")))).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Offer was not found."));
    if (!"accepted".equals(offer.getStatus()) || !offer.getBuyer().getId().equals(reviewer.getId())) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the buyer can review a completed deal.");
    if (reviews.existsByOfferId(offer.getId())) throw new ResponseStatusException(HttpStatus.CONFLICT, "This deal has already been reviewed.");
    User seller = offer.getSeller();
    int rating = ((Number) body.getOrDefault("rating", 0)).intValue();
    if (rating < 1 || rating > 5) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rating must be between 1 and 5.");
    Review review = new Review(); review.setOffer(offer); review.setReviewer(reviewer); review.setTargetUser(seller); review.setRating(rating); review.setComment(String.valueOf(body.getOrDefault("comment", ""))); review = reviews.save(review);
    int oldCount = seller.getRatingsCount(); seller.setRating(((seller.getRating() * oldCount) + rating) / (oldCount + 1)); seller.setRatingsCount(oldCount + 1); users.save(seller);
    return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("success",true,"data",review));
  }
  @GetMapping("/user/{id}") public Map<String,Object> userReviews(@PathVariable UUID id){return Map.of("success",true,"data",reviews.findByTargetUserIdOrderByCreatedAtDesc(id));}
  @PostMapping("/{id}/respond") public Map<String,Object> respond(@PathVariable UUID id,@RequestBody Map<String,String> body,Authentication a){Review r=reviews.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));if(!r.getTargetUser().getId().equals(user(a).getId()))throw new ResponseStatusException(HttpStatus.FORBIDDEN);r.setResponse(body.getOrDefault("response",""));return Map.of("success",true,"data",reviews.save(r));}
  private User user(Authentication a){if(a==null||!(a.getPrincipal() instanceof User u))throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);return u;}
}
