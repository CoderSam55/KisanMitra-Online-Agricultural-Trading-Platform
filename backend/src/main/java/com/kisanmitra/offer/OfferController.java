package com.kisanmitra.offer;

import com.kisanmitra.listing.Listing;
import com.kisanmitra.listing.ListingRepository;
import com.kisanmitra.notification.Notification;
import com.kisanmitra.notification.NotificationRepository;
import com.kisanmitra.order.Order;
import com.kisanmitra.order.OrderRepository;
import com.kisanmitra.chat.*;
import com.kisanmitra.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.*;

@RestController @RequestMapping("/api/offers") @RequiredArgsConstructor
public class OfferController {
  private final OfferRepository offers; private final ListingRepository listings; private final NotificationRepository notifications; private final OrderRepository orders; private final ConversationRepository conversations; private final ChatMessageRepository chatMessages;

  @PostMapping @Transactional
  public ResponseEntity<?> create(@RequestBody Map<String,Object> body, Authentication authentication) {
    User buyer = current(authentication);
    Listing listing = listings.findById(UUID.fromString(String.valueOf(body.get("listing")))).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Listing was not found."));
    if (!"active".equals(listing.getStatus())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This listing is no longer available.");
    if (listing.getSeller().getId().equals(buyer.getId())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot make an offer on your own listing.");
    double quantity = ((Number) body.getOrDefault("quantity", 0)).doubleValue();
    if (quantity <= 0 || quantity > listing.getQuantity()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Requested quantity is not available.");
    Offer offer = new Offer(); offer.setListing(listing); offer.setBuyer(buyer); offer.setSeller(listing.getSeller()); offer.setPrice(((Number)body.getOrDefault("price", 0)).doubleValue()); offer.setQuantity(quantity); offer.setUnit(String.valueOf(body.getOrDefault("unit", listing.getUnit()))); offer.setMessage(String.valueOf(body.getOrDefault("message", ""))); if (body.get("rentalStart") != null) offer.setRentalStart(LocalDate.parse(String.valueOf(body.get("rentalStart")))); if (body.get("rentalEnd") != null) offer.setRentalEnd(LocalDate.parse(String.valueOf(body.get("rentalEnd"))));
    Offer saved = offers.save(offer); listing.setOffersCount(listing.getOffersCount() + 1); listings.save(listing);
    postOfferMessage(offer, buyer.getName()+" made an offer: ₹"+offer.getPrice()+" per "+offer.getUnit()+" for "+offer.getQuantity()+" "+offer.getUnit()+(offer.getRentalStart()!=null?" (rental "+offer.getRentalStart()+" to "+offer.getRentalEnd()+")":""));
    notify(listing.getSeller(), "offer", "New offer received", buyer.getName() + " made an offer on " + listing.getTitle());
    return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("success", true, "data", saved));
  }

  @GetMapping("/me") public Map<String,Object> mine(Authentication authentication) { return Map.of("success",true,"data",offers.findByBuyerIdOrderByCreatedAtDesc(current(authentication).getId())); }
  @GetMapping("/listing/{id}") public Map<String,Object> listing(@PathVariable UUID id, Authentication authentication) { User user=current(authentication); Listing listing=listings.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND)); if(!listing.getSeller().getId().equals(user.getId())) throw new ResponseStatusException(HttpStatus.FORBIDDEN); return Map.of("success",true,"data",offers.findByListingIdOrderByCreatedAtDesc(id)); }

  @PostMapping("/{id}/reject") @Transactional
  public Map<String,Object> reject(@PathVariable UUID id, Authentication authentication) { Offer offer=owned(id,current(authentication)); if(!"pending".equals(offer.getStatus())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Offer is no longer pending."); offer.setStatus("rejected"); Offer saved=offers.save(offer); notify(offer.getBuyer(),"offer_rejected","Offer declined","Your offer on "+offer.getListing().getTitle()+" was declined."); return Map.of("success",true,"data",saved); }

  @PostMapping("/{id}/accept") @Transactional
  public Map<String,Object> accept(@PathVariable UUID id, Authentication authentication) { Offer offer=owned(id,current(authentication)); if(!"pending".equals(offer.getStatus())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Offer is no longer pending."); return Map.of("success",true,"data",complete(offer)); }

  @PostMapping("/{id}/counter") @Transactional
  public Map<String,Object> counter(@PathVariable UUID id, @RequestBody Map<String,Object> body, Authentication authentication) { User actor=current(authentication); Offer offer=offers.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND)); double price=((Number)body.getOrDefault("price",0)).doubleValue(); double quantity=((Number)body.getOrDefault("quantity",offer.getQuantity())).doubleValue(); if(price<=0||quantity<=0||quantity>offer.getListing().getQuantity()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Counter offer is not valid."); if(actor.getId().equals(offer.getSeller().getId()) && "pending".equals(offer.getStatus())) { offer.setCounterPrice(price); offer.setCounterQuantity(quantity); offer.setStatus("countered"); Offer saved=offers.save(offer); postOfferMessage(offer,"Seller made a counter offer: ₹"+price+" per "+offer.getUnit()+" for "+quantity+" "+offer.getUnit()); notify(offer.getBuyer(),"counter_offer","Seller made a counter offer","Counter: ₹"+price+" per "+offer.getUnit()+" for "+quantity+" "+offer.getUnit()+" on "+offer.getListing().getTitle()+"."); return Map.of("success",true,"data",saved); } if(actor.getId().equals(offer.getBuyer().getId()) && "countered".equals(offer.getStatus())) { offer.setPrice(price); offer.setQuantity(quantity); offer.setCounterPrice(null); offer.setCounterQuantity(null); offer.setStatus("pending"); Offer saved=offers.save(offer); postOfferMessage(offer,"Buyer responded with a counter offer: ₹"+price+" per "+offer.getUnit()+" for "+quantity+" "+offer.getUnit()); notify(offer.getSeller(),"counter_offer","Buyer sent a counter offer","Buyer counter: ₹"+price+" per "+offer.getUnit()+" for "+quantity+" "+offer.getUnit()+" on "+offer.getListing().getTitle()+"."); return Map.of("success",true,"data",saved); } throw new ResponseStatusException(HttpStatus.FORBIDDEN,"This counter offer is not available."); }

  @PostMapping("/{id}/accept-counter") @Transactional
  public Map<String,Object> acceptCounter(@PathVariable UUID id, Authentication authentication) { User buyer=current(authentication); Offer offer=offers.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND)); if(!offer.getBuyer().getId().equals(buyer.getId())) throw new ResponseStatusException(HttpStatus.FORBIDDEN); if(!"countered".equals(offer.getStatus())||offer.getCounterPrice()==null||offer.getCounterQuantity()==null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"There is no pending counter offer."); offer.setPrice(offer.getCounterPrice()); offer.setQuantity(offer.getCounterQuantity()); return Map.of("success",true,"data",complete(offer)); }

  private Offer owned(UUID id, User seller) { Offer offer=offers.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND)); if(!offer.getSeller().getId().equals(seller.getId())) throw new ResponseStatusException(HttpStatus.FORBIDDEN); return offer; }
  private Offer complete(Offer offer) { Listing listing=offer.getListing(); if(!"active".equals(listing.getStatus())||offer.getQuantity()>listing.getQuantity()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"This listing is no longer available."); listing.setQuantity(Math.max(0,listing.getQuantity()-offer.getQuantity())); listing.setStatus("sold"); listings.save(listing); offer.setStatus("accepted"); Offer saved=offers.save(offer); Order order=new Order(); order.setOrderNumber("KM-"+UUID.randomUUID().toString().substring(0,8).toUpperCase()); order.setListing(listing); order.setBuyer(offer.getBuyer()); order.setSeller(offer.getSeller()); order.setAgreedPrice(offer.getPrice()); order.setQuantity(offer.getQuantity()); order.setUnit(offer.getUnit()); order.setTotalAmount(offer.getPrice()*offer.getQuantity()); orders.save(order); postOfferMessage(offer,"Offer accepted. Order ID: "+order.getOrderNumber()); notify(offer.getBuyer(),"offer_accepted","Offer accepted","Your offer on "+listing.getTitle()+" was accepted. Order ID: "+order.getOrderNumber()+". Please rate the seller."); return saved; }
  private void postOfferMessage(Offer offer,String text){Conversation c=conversations.findBetween(offer.getBuyer().getId(),offer.getSeller().getId()).stream().findFirst().orElseGet(()->{Conversation created=new Conversation();created.setParticipantOne(offer.getBuyer());created.setParticipantTwo(offer.getSeller());created.setListing(offer.getListing());return conversations.save(created);});ChatMessage message=new ChatMessage();message.setConversation(c);message.setSender(offer.getBuyer());message.setText(text);chatMessages.save(message);}
  private void notify(User recipient,String type,String title,String message){ Notification n=new Notification(); n.setRecipient(recipient); n.setType(type); n.setTitle(title); n.setMessage(message); notifications.save(n); }
  private User current(Authentication a){ if(a==null||!(a.getPrincipal() instanceof User u))throw new ResponseStatusException(HttpStatus.UNAUTHORIZED); return u; }
}
