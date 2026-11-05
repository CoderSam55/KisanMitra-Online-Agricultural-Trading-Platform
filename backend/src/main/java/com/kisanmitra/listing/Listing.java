package com.kisanmitra.listing;

import com.kisanmitra.user.User;
import jakarta.persistence.*;
import lombok.Getter; import lombok.NoArgsConstructor; import lombok.Setter;
import java.time.Instant; import java.util.UUID;

@Entity @Table(name="listings") @Getter @Setter @NoArgsConstructor
public class Listing {
    @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
    @Column(nullable=false) private String title;
    @Column(length=3000) private String description;
    @Column(nullable=false) private String category;
    private String subCategory; private String transactionType = "sell";
    private double price; private String unit = "KG"; private double quantity; private boolean negotiable = true;
    @Column(columnDefinition="text") private String images; private String video; private String variety; private String harvestDate; private String grade; private boolean organic;
    private String state; private String district; private String taluka; private String village;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) private User seller;
    private String status = "active"; private long views; private long offersCount;
    private Instant createdAt=Instant.now(); private Instant updatedAt=Instant.now();
    @PreUpdate void touch(){updatedAt=Instant.now();}
}
