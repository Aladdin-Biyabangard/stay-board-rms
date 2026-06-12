package az.aladdin.stayboard.entity;

import az.aladdin.stayboard.annotation.NoFieldLogging;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@EqualsAndHashCode(of = "id")
@AllArgsConstructor
@NoArgsConstructor
@ToString(of = "id")
@Builder
@Getter
@Setter
@Entity
@Table(name = "order_item_modifiers")
@NoFieldLogging
public class OrderItemModifierEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long hotelId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id")
    private OrderItemEntity orderItem;

    private Long modifierGroupId;

    @Column(name = "option_name")
    private String modifierName;

    @Column(precision = 12, scale = 2)
    private BigDecimal priceDelta;
}
