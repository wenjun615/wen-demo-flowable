package com.wen.flowable;

import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
class FlowableApplicationTests {

    private static final Logger logger = LoggerFactory.getLogger(FlowableApplicationTests.class);

    @Autowired
    RuntimeService runtimeService;

    @Autowired
    TaskService taskService;

    private static final HashMap<String, Object> variables = new HashMap<>();

    static {
        variables.put("leaveUser", "1");
        variables.put("groupLeader", "2");
        variables.put("manager", "3");
        variables.put("processDefinitionKey", "ask_for_leave");
    }

    /**
     * 开启一个流程
     */
    @Test
    void startProcessInstance() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(
                (String) variables.get("processDefinitionKey"),
                variables
        );
        logger.info("创建请假流程：processInstanceId={}", processInstance.getId());
    }

    /**
     * 提交给组长审批
     */
    @Test
    public void submitToGroupLeader() {
        List<Task> taskList = taskService.createTaskQuery()
                .taskAssignee((String) variables.get("leaveUser"))
                .orderByTaskId()
                .desc()
                .list();
        for (Task task : taskList) {
            logger.info("任务 ID：{}；任务处理人：{}；任务是否挂起：{}", task.getId(), task.getAssignee(), task.isSuspended());
            Map<String, Object> map = new HashMap<>();
            map.put("groupLeader", variables.get("groupLeader"));
            taskService.complete(task.getId(), map);
        }
    }

    /**
     * 组长审批批准
     */
    @Test
    void groupLeaderApprove() {
        List<Task> list = taskService.createTaskQuery()
                .taskAssignee((String) variables.get("groupLeader"))
                .orderByTaskId()
                .desc()
                .list();
        for (Task task : list) {
            logger.info("组长 {} 在审批 {} 任务", task.getAssignee(), task.getId());
            Map<String, Object> map = new HashMap<>();
            map.put("manager", variables.get("manager"));
            map.put("checkResult", "通过");
            taskService.complete(task.getId(), map);
        }
    }

    /**
     * 组长审批拒绝
     */
    @Test
    void groupLeaderReject() {
        List<Task> list = taskService.createTaskQuery()
                .taskAssignee((String) variables.get("groupLeader"))
                .orderByTaskId()
                .desc()
                .list();
        for (Task task : list) {
            logger.info("组长 {} 在审批 {} 任务", task.getAssignee(), task.getId());
            Map<String, Object> map = new HashMap<>();
            map.put("checkResult", "拒绝");
            taskService.complete(task.getId(), map);
        }
    }

    /**
     * 经理审批批准
     */
    @Test
    void managerApprove() {
        List<Task> list = taskService.createTaskQuery()
                .taskAssignee((String) variables.get("manager"))
                .orderByTaskId()
                .desc()
                .list();
        for (Task task : list) {
            logger.info("经理 {} 在审批 {} 任务", task.getAssignee(), task.getId());
            Map<String, Object> map = new HashMap<>();
            map.put("checkResult", "通过");
            taskService.complete(task.getId(), map);
        }
    }

    /**
     * 经理审批拒绝
     */
    @Test
    void managerReject() {
        List<Task> list = taskService.createTaskQuery()
                .taskAssignee((String) variables.get("manager"))
                .orderByTaskId()
                .desc()
                .list();
        for (Task task : list) {
            logger.info("经理 {} 在审批 {} 任务", task.getAssignee(), task.getId());
            Map<String, Object> map = new HashMap<>();
            map.put("checkResult", "拒绝");
            taskService.complete(task.getId(), map);
        }
    }

}
