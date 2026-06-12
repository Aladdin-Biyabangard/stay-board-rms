package az.aladdin.stayboard.entity;

import az.aladdin.stayboard.annotation.NoFieldLogging;
import az.aladdin.stayboard.model.enums.WaitlistStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Enumerated;
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
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import static jakarta.persistence.EnumType.STRING;

@EqualsAndHashCode(of = "id")
@AllArgsConstructor
@NoArgsConstructor
@ToString(of = "id")
@Builder
@Getter
@Setter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "waitlist_entries")
@NoFieldLogging
public class WaitlistEntryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long hotelId;

    @Column(nullable = false)
    private Integer partySize;

    @Enumerated(STRING)
    @Column(nullable = false)
    private WaitlistStatus status;

    @Embedded
    private ReservationMainInfo reservationMainInfo;

    private String notes;

    private Integer estimatedWaitMinutes;

    @ManyToOne
    @JoinColumn(name = "preferred_table_id")
    private TableEntity preferredTable;

    @ManyToOne
    @JoinColumn(name = "seated_table_id")
    private TableEntity seatedTable;

    @CreatedDate
    @Column(updatable = false)
    private java.time.LocalDateTime createdAt;

    @CreatedBy
    private String createdBy;

    @LastModifiedDate
    private java.time.LocalDateTime updatedAt;

    @LastModifiedBy
    private String updatedBy;
}
