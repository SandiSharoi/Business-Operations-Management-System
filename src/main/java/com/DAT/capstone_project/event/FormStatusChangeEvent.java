package com.DAT.capstone_project.event;

import com.DAT.capstone_project.model.FormApplyEntity;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class FormStatusChangeEvent extends ApplicationEvent {
    private final FormApplyEntity formApply;

    public FormStatusChangeEvent(Object source, FormApplyEntity formApply) {
        super(source);
        this.formApply = formApply;
    }
}
