package server.poptato.todo.application;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.SimpleTriggerContext;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
@SpringBootTest
class TodoSchedulerTest {
    @Autowired
    TodoScheduler todoScheduler;
    @Test
    @DisplayName("updateType 메서드가 매일 자정에 실행되어야 한다")
    public void shouldTrigger_updateType_atEveryMidNight() throws ParseException {
        // Given - 상황 설정
        String cronExpression = "0 0 0 * * *"; // 자정에 실행되는 cron 표현식
        CronTrigger trigger = new CronTrigger(cronExpression);
        Date startTime = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").parse("2023/12/19 23:59:50");
        SimpleTriggerContext context = new SimpleTriggerContext();
        context.update(startTime, startTime, startTime);

        // 예상되는 실행 시간 목록
        String expectedTime = "2023/12/20 00:00:00";

        Date nextExecutionTime = trigger.nextExecutionTime(context);

        // Then - 결과 검증
        String actualTime = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(nextExecutionTime);

        // 타입 불일치를 해결하기 위해 문자열 비교를 위한 Matcher 사용
        Assertions.assertThat(actualTime).isEqualTo(expectedTime); // 여기서 `is`는 문자열을 비교할 때 사용
        context.update(nextExecutionTime, nextExecutionTime, nextExecutionTime);
    }
}