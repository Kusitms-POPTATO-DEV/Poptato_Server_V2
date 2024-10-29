package server.poptato.todo.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import server.poptato.todo.application.response.*;
import server.poptato.todo.converter.TodoDtoConverter;
import server.poptato.todo.domain.entity.Todo;
import server.poptato.todo.domain.repository.TodoRepository;
import server.poptato.todo.domain.value.TodayStatus;
import server.poptato.todo.domain.value.Type;
import server.poptato.user.validator.UserValidator;

import java.util.List;
import java.util.stream.Collectors;

@Transactional
@RequiredArgsConstructor
@Service
public class TodoBacklogService {
    private final TodoRepository todoRepository;
    private final UserValidator userValidator;

    public BacklogListResponseDto getBacklogList(Long userId, int page, int size) {
        userValidator.checkIsExistUser(userId);

        Page<Todo> backlogs = getBacklogsPagination(userId, page, size);

        return TodoDtoConverter.toBacklogListDto(backlogs);
    }

    public BacklogCreateResponseDto generateBacklog(Long userId, String content) {
        userValidator.checkIsExistUser(userId);
        Integer maxBacklogOrder = todoRepository.findMaxBacklogOrderByUserIdOrZero(userId);
        Todo backlog = Todo.createBacklog(userId, content, maxBacklogOrder + 1);
        Todo newBacklog = todoRepository.save(backlog);
        return BacklogCreateResponseDto.builder()
                .todoId(newBacklog.getId())
                .build();
    }
    public PaginatedHistoryResponseDto getHistories(Long userId, int page, int size) {
        userValidator.checkIsExistUser(userId);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "completedDateTime"));

        // 유저 아이디와 completedDate가 null이 아닌 것들을 가져옴
        Page<Todo> todosPage = todoRepository.findByUserIdAndCompletedDateTimeIsNotNull(userId, pageable);

        List<HistoryResponseDto> histories = todosPage.getContent().stream()
                .map(todo -> new HistoryResponseDto(
                        todo.getId(),
                        todo.getContent(),
                        todo.getCompletedDateTime().toLocalDate()  // 날짜를 LocalDate로 변환
                ))
                .collect(Collectors.toList());

        return new PaginatedHistoryResponseDto(histories, todosPage.getTotalPages());
    }

    public PaginatedYesterdayResponseDto getYesterdays(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Todo> todosPage = todoRepository.findByUserIdAndTypeAndTodayStatus(userId, Type.YESTERDAY, TodayStatus.INCOMPLETE, pageable);

        List<YesterdayResponseDto> yesterdays = todosPage.getContent().stream()
                .map(todo -> new YesterdayResponseDto(
                        todo.getId(),
                        todo.getContent()
                ))
                .collect(Collectors.toList());

        return new PaginatedYesterdayResponseDto(yesterdays, todosPage.getTotalPages());
    }
    private Page<Todo> getBacklogsPagination(Long userId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        List<Type> types = List.of(Type.BACKLOG, Type.YESTERDAY);

        Page<Todo> backlogs = todoRepository.findBacklogsByUserId(userId, types, pageRequest);

        return backlogs;
    }
}
