package server.poptato.todo.application;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.SimpleTriggerContext;
import org.springframework.transaction.annotation.Transactional;
import server.poptato.todo.domain.entity.Todo;
import server.poptato.todo.domain.repository.TodoRepository;
import server.poptato.todo.domain.value.Type;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Transactional
@SpringBootTest
class TodoSchedulerTest {
    @Autowired
    TodoRepository todoRepository;
    @Autowired
    TodoScheduler todoScheduler;

    @Test
    @DisplayName("스케줄러가 매일 자정에 성공적으로 실행된다.")
    public void scheduler_cron_Success() throws ParseException {
        //given
        String cronExpression = "0 0 0 * * *";
        CronTrigger trigger = new CronTrigger(cronExpression);
        Date startTime = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").parse("2023/12/19 23:59:50");
        SimpleTriggerContext context = new SimpleTriggerContext();
        context.update(startTime, startTime, startTime);
        String expectedTime = "2023/12/20 00:00:00";
        Date nextExecutionTime = trigger.nextExecutionTime(context);

        //when & then
        String actualTime = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(nextExecutionTime);
        Assertions.assertThat(actualTime).isEqualTo(expectedTime);
        context.update(nextExecutionTime, nextExecutionTime, nextExecutionTime);
    }

    @Test
    @DisplayName("updateTodoType 메서드가 성공적으로 실행된다.")
    void updateTodoType_Success() {
        //when
        todoScheduler.updateTodoType();

        //then
        List<Todo> yesterdayTasks = todoRepository.findByType(Type.YESTERDAY);
        assertTrue(yesterdayTasks.stream().anyMatch(todo -> todo.getContent().equals("할 일 1")));
        assertTrue(yesterdayTasks.stream().anyMatch(todo -> todo.getContent().equals("할 일 2")));
        assertTrue(yesterdayTasks.stream().anyMatch(todo -> todo.getContent().equals("할 일 4")));
        assertTrue(yesterdayTasks.stream().anyMatch(todo -> todo.getContent().equals("할 일 5")));
        assertTrue(yesterdayTasks.stream().anyMatch(todo -> todo.getContent().equals("할 일 7")));
        assertTrue(yesterdayTasks.stream().anyMatch(todo -> todo.getContent().equals("할 일 8")));
        assertTrue(yesterdayTasks.stream().anyMatch(todo -> todo.getContent().equals("할 일 10")));
        assertTrue(yesterdayTasks.stream().anyMatch(todo -> todo.getContent().equals("할 일 12")));
        assertTrue(yesterdayTasks.stream().anyMatch(todo -> todo.getContent().equals("할 일 14")));
        assertTrue(yesterdayTasks.stream().anyMatch(todo -> todo.getContent().equals("할 일 16")));

        List<Todo> backlogTasks = todoRepository.findByType(Type.BACKLOG);
        assertTrue(backlogTasks.stream().anyMatch(todo -> todo.getContent().equals("할 일 1")));
        assertTrue(backlogTasks.stream().anyMatch(todo -> todo.getContent().equals("할 일 2")));
        assertTrue(backlogTasks.stream().anyMatch(todo -> todo.getContent().equals("할 일 3")));
        assertTrue(backlogTasks.stream().anyMatch(todo -> todo.getContent().equals("할 일 4")));
        assertTrue(backlogTasks.stream().anyMatch(todo -> todo.getContent().equals("할 일 5")));
        assertTrue(backlogTasks.stream().anyMatch(todo -> todo.getContent().equals("할 일 6")));
        assertTrue(backlogTasks.stream().anyMatch(todo -> todo.getContent().equals("할 일 7")));
        assertTrue(backlogTasks.stream().anyMatch(todo -> todo.getContent().equals("할 일 8")));
        assertTrue(backlogTasks.stream().anyMatch(todo -> todo.getContent().equals("할 일 9")));
        assertTrue(backlogTasks.stream().anyMatch(todo -> todo.getContent().equals("할 일 10")));
    }
}