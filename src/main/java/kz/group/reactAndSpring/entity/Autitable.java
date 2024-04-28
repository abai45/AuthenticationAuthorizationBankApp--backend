package kz.group.reactAndSpring.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import kz.group.reactAndSpring.domain.RequestContext;
import kz.group.reactAndSpring.exception.ApiException;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.util.AlternativeJdkIdGenerator;

import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(value = {"createdAt", "updatedAt"}, allowGetters = true)
public abstract class Autitable {
    @Id
    @SequenceGenerator(name = "primary_key_seq", sequenceName = "primary_key_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "primary_key_seq")
    @Column(name = "id", updatable = false)
    private Long id;
    private String referenceId = new AlternativeJdkIdGenerator().generateId().toString();
    @NotNull
    private Long createdBy;
    @NotNull
    private Long updatedBy;

    //    @OneToMany
//    @JoinColumn(
//            name="owner_id",
//            referencedColumnName = "id",
//            foreignKey = @ForeignKey(name = "fk_user_owner", value = ConstraintMode.CONSTRAINT)
//    )
//    private UserEntity owner;
//
    @NotNull
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @CreatedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void beforePersist() {
        var userId = 0L;//RequestContext.getUserId();
//        if(userId == null) {
//            throw new ApiException("Cannot persist entity without user ID in Request Context for the thread");
//        }
        setCreatedAt(LocalDateTime.now());
        setCreatedBy(userId);
        setUpdatedBy(userId);
        setUpdatedAt(LocalDateTime.now());
    }

    @PreUpdate
    public void beforeUpdate() {
        var userId = 0L;//RequestContext.getUserId();
//        if(userId == null) {
//            throw new ApiException("Cannot update entity without user ID in Request Context for the thread");
//        }
        setUpdatedAt(LocalDateTime.now());
        setUpdatedBy(userId);
    }
}
