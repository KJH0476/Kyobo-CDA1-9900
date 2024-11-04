package kyobo.cda.UserService.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @Comment("회원 고유 번호")
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Comment("회원 이메일")
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Comment("회원 비밀번호")
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Comment("회원명")
    @Column(name = "user_name", nullable = false, length = 100)
    private String username;

    @Comment("회원 전화번호")
    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Comment("회원 가입일")
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Comment("회원 정보 수정일")
    @CreationTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Comment("회원 권한")
    @Enumerated(EnumType.STRING)
    @Column(name="role", nullable = false)
    private Role role;

    @PrePersist
    public void prePersist() {
        id = UUID.randomUUID();
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
