package com.kisanmitra.listing;

import com.kisanmitra.user.User;
import jakarta.persistence.*;
import lombok.Getter; import lombok.NoArgsConstructor; import lombok.Setter;
import java.time.Instant; import java.util.UUID; import java.util.*; import com.fasterxml.jackson.core.type.TypeReference; import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity @Table(name="listings") @Getter @Setter @NoArgsConstructor
public class Listing {
    @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
    @Column(nullable=false) private String title;
    @Column(length=3000) private String description;
    @Column(nullable=false) private String category;
    private String subCategory; private String transactionType = "sell";
    private double price; private String unit = "KG"; private double quantity; private boolean negotiable = true;
    @Column(name="images", columnDefinition="text") @com.fasterxml.jackson.annotation.JsonIgnore private String imagesJson; private String video; private String variety; private String harvestDate; private String grade; private boolean organic;
    private String brand; private String model; private String year; private String condition;
    @Transient public List<String> getImages(){ try { return imagesJson==null||imagesJson.isBlank()?List.of():new ObjectMapper().readValue(imagesJson,new TypeReference<List<String>>(){}); } catch(Exception e){ return imagesJson==null?List.of():List.of(imagesJson); } }
    public void setImages(List<String> images){ try { this.imagesJson=new ObjectMapper().writeValueAsString(images==null?List.of():images); } catch(Exception e){ this.imagesJson="[]"; } }
    private String state; private String district; private String taluka; private String village;
    @Transient @JsonProperty("location") public Map<String,String> getLocation(){
        Map<String,String> m=new LinkedHashMap<>(); m.put("state",state); m.put("district",district); m.put("taluka",taluka); m.put("village",village); return m;
    }
    @ManyToOne(fetch=FetchType.EAGER, optional=false) private User seller;
    private String status = "active"; private long views; private long offersCount;
    private Instant createdAt=Instant.now(); private Instant updatedAt=Instant.now();
    @PreUpdate void touch(){updatedAt=Instant.now();}
}
