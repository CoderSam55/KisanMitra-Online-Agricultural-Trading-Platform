package com.kisanmitra.listing;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface ListingRepository extends JpaRepository<Listing, UUID> {
    List<Listing> findBySellerIdOrderByCreatedAtDesc(UUID sellerId);
    List<Listing> findByStatusOrderByCreatedAtDesc(String status);
}
