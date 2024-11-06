package server.poptato.todo.constant;

import java.util.Arrays;
import java.util.List;

public class TutorialMessage {
    public static final String BACKLOG_NEW_TODO = "'새로 추가하기'를 눌러 할 일을 생성해 보세요.";
    public static final String BACKLOG_BOOKMARK_DDAY = "... 을 눌러 '중요'와 '마감기한'을 설정해 보세요.";
    public static final String BACKLOG_DRAG_AND_DROP = "할 일을 꾹 눌러 위 아래로 옮겨 보세요.";
    public static final String BACKLOG_LEFT_SWIPE = "할 일을 왼쪽으로 넘겨보세요. '오늘'로 할 일이 이동해요.";
    public static final String TODAY_COMPLETE = "오늘의 할 일을 모두 체크해 보세요!";

    public static final List<String> BACKLOG_MESSAGES = Arrays.asList(
            BACKLOG_NEW_TODO,
            BACKLOG_BOOKMARK_DDAY,
            BACKLOG_DRAG_AND_DROP,
            BACKLOG_LEFT_SWIPE
    );

}
