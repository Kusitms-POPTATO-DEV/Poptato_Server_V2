package server.poptato.todo.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import server.poptato.todo.api.request.BacklogCreateRequestDto;
import server.poptato.todo.application.response.BacklogCreateResponseDto;
import server.poptato.todo.application.response.BacklogListResponseDto;
import server.poptato.todo.application.response.PaginatedHistoryResponseDto;
import server.poptato.todo.application.response.PaginatedYesterdayResponseDto;
import server.poptato.todo.converter.TodoDtoConverter;
import server.poptato.todo.domain.entity.Todo;
import server.poptato.todo.domain.repository.TodoRepository;
import server.poptato.todo.domain.value.TodayStatus;
import server.poptato.todo.domain.value.Type;
import server.poptato.user.validator.UserValidator;

import java.time.LocalDate;
import java.util.List;

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

    public BacklogCreateResponseDto generateBacklog(Long userId, BacklogCreateRequestDto backlogCreateRequestDto) {
        userValidator.checkIsExistUser(userId);
        Integer maxBacklogOrder = todoRepository.findMaxBacklogOrderByUserIdOrZero(userId);
        Todo newBacklog = createNewBacklog(userId, backlogCreateRequestDto, maxBacklogOrder);
        return TodoDtoConverter.toBacklogCreateDto(newBacklog);
    }

    public PaginatedHistoryResponseDto getHistories(Long userId, int page, int size, LocalDate date) {
        userValidator.checkIsExistUser(userId);
        Page<Todo> historiesPage = getHistoriesPage(userId, page, size, date);
        return TodoDtoConverter.toHistoryListDto(historiesPage);
    }

    private Page<Todo> getHistoriesPage(Long userId, int page, int size, LocalDate date) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "completedDateTime"));
        Page<Todo> historiesPage = todoRepository.findHistories(userId, TodayStatus.COMPLETED,date, pageable);
        return historiesPage;
    }

    public PaginatedYesterdayResponseDto getYesterdays(Long userId, int page, int size) {
        userValidator.checkIsExistUser(userId);

        Pageable pageable = PageRequest.of(page, size);
        Page<Todo> yesterdaysPage = todoRepository.findByUserIdAndTypeAndTodayStatus(userId, Type.YESTERDAY, TodayStatus.INCOMPLETE, pageable);

        return TodoDtoConverter.toYesterdayListDto(yesterdaysPage);
    }

    private Page<Todo> getBacklogsPagination(Long userId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        List<Type> types = List.of(Type.BACKLOG, Type.YESTERDAY);
        List<TodayStatus> statuses = List.of(TodayStatus.COMPLETED);
        Page<Todo> backlogs = todoRepository.findBacklogsByUserId(userId, types, statuses, pageRequest);

        return backlogs;
    }

    private Todo createNewBacklog(Long userId, BacklogCreateRequestDto backlogCreateRequestDto, Integer maxBacklogOrder) {
        Todo backlog = Todo.createBacklog(userId, backlogCreateRequestDto.getContent(), maxBacklogOrder + 1);
        Todo newBacklog = todoRepository.save(backlog);
        return newBacklog;
    }
}
