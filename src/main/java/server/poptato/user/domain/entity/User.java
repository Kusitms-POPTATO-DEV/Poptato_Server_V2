package server.poptato.user.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String kakaoId;

    @NotNull
    private String name;

    @NotNull
    private String email;

    @CreatedDate  // 엔티티가 처음 생성될 때 시간 자동 저장
    @Column(updatable = false)  // 생성일은 수정 불가
    private LocalDateTime createDate;

    @LastModifiedDate  // 엔티티가 수정될 때 시간 자동 저장
    private LocalDateTime modifyDate;

}
