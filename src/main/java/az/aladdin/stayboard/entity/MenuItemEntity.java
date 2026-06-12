package az.aladdin.stayboard.entity;

import az.aladdin.stayboard.annotation.NoFieldLogging;
import az.aladdin.stayboard.model.enums.SaleUnitType;
import az.aladdin.stayboard.model.enums.TaxType;
import az.aladdin.stayboard.persistence.SaleUnitTypeConverter;
import jakarta.persistence.Convert;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@EqualsAndHashCode(of = "id")
@AllArgsConstructor
@NoArgsConstructor
@ToString(of = "id")
@Builder
@Getter
@Setter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "menu_items")
@NoFieldLogging
public class MenuItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long hotelId;

    private String itemName;

    private String itemDescription;

    private boolean active;

    @Column(precision = 12, scale = 2)
    private BigDecimal price;

    @Column(precision = 12, scale = 2)
    private BigDecimal taxRate;

    @Enumerated(EnumType.STRING)
    private TaxType taxType;

    @Convert(converter = SaleUnitTypeConverter.class)
    @Builder.Default
    private SaleUnitType saleUnitType = SaleUnitType.PIECE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_category_id")
    private MenuCategoryEntity menuCategory;

    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name = "photo_url")
    @Builder.Default
    private Set<String> photoUrls = new HashSet<>();

    private String mainImageUrl;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "menu_item_allergens",
            joinColumns = @JoinColumn(name = "menu_item_id"),
            inverseJoinColumns = @JoinColumn(name = "allergen_id")
    )
    @Builder.Default
    private Set<AllergenEntity> allergens = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "menu_item_dietary_tags",
            joinColumns = @JoinColumn(name = "menu_item_id"),
            inverseJoinColumns = @JoinColumn(name = "dietary_tag_id")
    )
    @Builder.Default
    private Set<DietaryTagEntity> dietaryTags = new HashSet<>();

    @OneToMany(mappedBy = "menuItem", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("sortOrder ASC")
    @Builder.Default
    private List<MenuItemModifierGroupEntity> modifierGroupLinks = new ArrayList<>();

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @CreatedBy
    private String createdBy;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @LastModifiedBy
    private String updatedBy;
}
