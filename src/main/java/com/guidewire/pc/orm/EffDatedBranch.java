package com.guidewire.pc.orm;

import java.util.Date;
import java.util.List;

public interface EffDatedBranch {
    Long getBranchId();
    String getPolicyNumber();
    String getJobNumber();
    String getJobType(); // Submission, PolicyChange, Cancellation, Renewal
    Date getPeriodStart();
    Date getPeriodEnd();
    Date getEditEffectiveDate();
    
    List<EffDatedBean> getEffDatedBeans();
    void addEffDatedBean(EffDatedBean bean);
}
