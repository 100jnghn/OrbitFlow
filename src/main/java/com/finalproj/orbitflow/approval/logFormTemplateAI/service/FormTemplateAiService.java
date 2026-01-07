package com.finalproj.orbitflow.approval.logFormTemplateAI.service;

import com.finalproj.orbitflow.approval.aiClient.FormDesignAiClient;
import com.finalproj.orbitflow.approval.logFormTemplateAI.entity.LogFormTemplateAI;
import com.finalproj.orbitflow.approval.logFormTemplateAI.enums.AiStatus;
import com.finalproj.orbitflow.approval.logFormTemplateAI.prompt.FormPromptBuilder;
import com.finalproj.orbitflow.approval.logFormTemplateAI.repository.LogFormTemplateAiRepository;
import com.finalproj.orbitflow.global.exception.NotFoundException;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : FormTemplateAiService
 * @since : 26. 1. 7. 수요일
 **/


@Service
@RequiredArgsConstructor
public class FormTemplateAiService {

    private final FormDesignAiClient formDesignAiClient;
    private final LogFormTemplateAiRepository logFormTemplateAiRepository;
    private final EmployeeRepository employeeRepository;

    public String generateFormTemplatePrompt(
            String formName,
            String purpose
    ) {
        return new FormPromptBuilder()
                .base()
                .components()
                .responseFormat()
                .userRequest(formName, purpose)
                .build();
    }

    @Transactional
    public String requestFormTemplate(
            Long employeeId,
            String formName,
            String purpose
    ) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new NotFoundException("No employee with id: " + employeeId));

        String prompt = generateFormTemplatePrompt(formName, purpose);

        String result = formDesignAiClient.completeFormDesign(prompt);

        LogFormTemplateAI.builder()
                .company(employee.getCompany())
                .prompt(prompt)
                .generatedTemplateJson(result)
                .build();


        logFormTemplateAiRepository.save(LogFormTemplateAI.builder()
                .company(employee.getCompany())
                .prompt(prompt)
                .generatedTemplateJson(result)
                .requestContext(result)
                .status(AiStatus.SUCCESS)
                .build());

        return result;
    }
}
