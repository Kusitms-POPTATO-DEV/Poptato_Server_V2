package server.poptato.todo.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.lang.Nullable;
import server.poptato.todo.domain.value.TodayStatus;
import server.poptato.todo.domain.value.Type;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Entity
@Builder
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Todo{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    private Long userId;
    @NotNull
    @Enumerated(EnumType.STRING)
    private Type type;
    @NotNull
    private String content;
    @Nullable
    private LocalDate deadline;
    @NotNull
    private boolean isBookmark;
    @Nullable
    private LocalDate todayDate;
    @Enumerated(EnumType.STRING)
    private TodayStatus todayStatus;
    @Nullable
    private Integer todayOrder;
    @Nullable
    private Integer backlogOrder;
    @Nullable
    private LocalDateTime completedDateTime;
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createDate;
    @LastModifiedDate
    private LocalDateTime modifyDate;
}
