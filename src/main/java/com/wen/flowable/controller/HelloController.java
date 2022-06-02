package com.wen.flowable.controller;

import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.*;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.image.ProcessDiagramGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

@RestController
public class HelloController {

    @Autowired
    RuntimeService runtimeService;

    @Autowired
    TaskService taskService;

    @Autowired
    RepositoryService repositoryService;

    @Autowired
    ProcessEngine processEngine;

    @GetMapping("/showPicture")
    public void showPicture(HttpServletResponse response, String processInstanceId) throws Exception {
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
        if (processInstance == null) {
            return;
        }
        List<Execution> executionList = runtimeService.createExecutionQuery()
                .processInstanceId(processInstanceId)
                .list();
        List<String> activityIdList = new ArrayList<>();
        for (Execution execution : executionList) {
            List<String> activeActivityIds = runtimeService.getActiveActivityIds(execution.getId());
            activityIdList.addAll(activeActivityIds);
        }
        // 生成流程图
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processInstance.getProcessDefinitionId());
        ProcessEngineConfiguration config = processEngine.getProcessEngineConfiguration();
        ProcessDiagramGenerator generator = config.getProcessDiagramGenerator();
        int len;
        byte[] b = new byte[1024];
        try (InputStream in = generator.generateDiagram(bpmnModel, "png", activityIdList, new ArrayList<>(),
                config.getActivityFontName(), config.getLabelFontName(), config.getAnnotationFontName(),
                config.getClassLoader(), 1.0, Boolean.FALSE); OutputStream out = response.getOutputStream()) {
            while ((len = in.read(b)) != -1) {
                out.write(b, 0, len);
            }
        }
    }
}