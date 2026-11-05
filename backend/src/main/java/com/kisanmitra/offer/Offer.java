package com.kisanmitra.offer;
import com.kisanmitra.listing.Listing; import com.kisanmitra.user.User; import jakarta.persistence.*; import lombok.*; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="offers") @Getter @Setter @NoArgsConstructor public class Offer { @Id @GeneratedValue(strategy=GenerationType.UUID) UUID id; @ManyToOne(optional=false) Listing listing; @ManyToOne(optional=false) User buyer; @ManyToOne(optional=false) User seller; double price; double quantity; String unit="KG"; String message; String status="pending"; Instant createdAt=Instant.now(); }
