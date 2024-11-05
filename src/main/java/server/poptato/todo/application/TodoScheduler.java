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
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void updateTodoType() {
        List<Long> updatedTodoIds = new ArrayList<>();
        Map<Long, List<Todo>> userIdAndIncompleteTodaysMap = changeIncompleteTodayToYesterday(updatedTodoIds);
        List<Todo> yesterdayIncompleteTodos = changeIncompleteYesterdayToBacklog(updatedTodoIds);
        save(userIdAndIncompleteTodaysMap, yesterdayIncompleteTodos);
    }

    private Map<Long, List<Todo>> changeIncompleteTodayToYesterday(List<Long> updatedTodoIds) {
        Map<Long, List<Todo>> userIdAndIncompleteTodaysMap = todoRepository.findByTypeAndTodayStatus(Type.TODAY, TodayStatus.INCOMPLETE)
                .stream()
                .collect(Collectors.groupingBy(Todo::getUserId));

        userIdAndIncompleteTodaysMap.forEach((userId, todos) -> {
            Integer minBacklogOrder = todoRepository.findMinBacklogOrderByUserIdOrZero(userId);
            int startingOrder = minBacklogOrder - 1;

            for (Todo todo : todos) {
                todo.setType(Type.YESTERDAY);
                todo.setBacklogOrder(startingOrder--);
                updatedTodoIds.add(todo.getId());
            }
        });
        return userIdAndIncompleteTodaysMap;
    }

    private List<Todo> changeIncompleteYesterdayToBacklog(List<Long> updatedTodoIds) {
        List<Todo> yesterdayIncompleteTodos = todoRepository.findByTypeAndTodayStatus(Type.YESTERDAY, TodayStatus.INCOMPLETE)
                .stream()
                .filter(todo -> !updatedTodoIds.contains(todo.getId()))
                .collect(Collectors.toList());

        yesterdayIncompleteTodos.forEach(todo -> {
            todo.setType(Type.BACKLOG);
            todo.setTodayStatus(null);
        });
        return yesterdayIncompleteTodos;
    }

    private void save(Map<Long, List<Todo>> userIdAndIncompleteTodaysMap, List<Todo> yesterdayIncompleteTodos) {
        for (Todo todo : userIdAndIncompleteTodaysMap.values().stream().flatMap(List::stream).collect(Collectors.toList())) {
            todoRepository.save(todo);
        }
        for (Todo todo : yesterdayIncompleteTodos) {
            todoRepository.save(todo);
        }
    }
}

