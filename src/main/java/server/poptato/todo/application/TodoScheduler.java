package server.poptato.todo.application;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import server.poptato.todo.domain.entity.Todo;
import server.poptato.todo.domain.repository.TodoRepository;
import server.poptato.todo.domain.value.TodayStatus;
import server.poptato.todo.domain.value.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TodoScheduler {
    private final TodoRepository todoRepository;
    @Scheduled(cron = "0 0 0 * * *") // 매일 자정에 실행
    @Transactional
    public void updateTodoType() {
        // 1. TODAY 상태에서 INCOMPLETE인 할 일들을 YESTERDAY로 전환 (사용자별로 처리)
        Map<Long, List<Todo>> todayIncompleteTodosByUser = todoRepository.findByTypeAndTodayStatus(Type.TODAY, TodayStatus.INCOMPLETE)
                .stream()
                .collect(Collectors.groupingBy(Todo::getUserId)); // 사용자별로 그룹화

        List<Long> updatedTodoIds = new ArrayList<>();  // YESTERDAY로 전환된 할일들의 ID 저장

        todayIncompleteTodosByUser.forEach((userId, todos) -> {
            Integer minBacklogOrder = todoRepository.findMinBacklogOrderByUserIdOrZero(userId);
            int startingOrder = minBacklogOrder - 1;

            for (Todo todo : todos) {
                todo.setType(Type.YESTERDAY);
                todo.setBacklogOrder(startingOrder--);
                updatedTodoIds.add(todo.getId());  // YESTERDAY로 전환된 항목의 ID 추가
            }
        });

        // 2. YESTERDAY 상태에서 INCOMPLETE인 할 일들을 BACKLOG로 전환 (BacklogOrder 유지)
        // 첫 번째 단계에서 YESTERDAY로 전환된 항목을 제외하고 처리
        List<Todo> yesterdayIncompleteTodos = todoRepository.findByTypeAndTodayStatus(Type.YESTERDAY, TodayStatus.INCOMPLETE)
                .stream()
                .filter(todo -> !updatedTodoIds.contains(todo.getId()))  // 이미 YESTERDAY로 바뀐 항목 제외
                .collect(Collectors.toList());

        yesterdayIncompleteTodos.forEach(todo -> {
            todo.setType(Type.BACKLOG);
            todo.setTodayStatus(null);
        });

        // 3. 저장
        for (Todo todo : todayIncompleteTodosByUser.values().stream().flatMap(List::stream).collect(Collectors.toList())) {
            todoRepository.save(todo);
        }
        for (Todo todo : yesterdayIncompleteTodos) {
            todoRepository.save(todo);
        }
    }
}

