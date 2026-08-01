package gw.pc.job

import com.guidewire.pc.model.AuditInformation
import com.guidewire.pc.model.PolicyPeriod
import gw.pc.config.PCConstants
import gw.pc.rating.AuditRatingEngine
import java.math.BigDecimal
import java.time.LocalDate

class AuditJobService {

  public static function startAudit(period : PolicyPeriod, auditType : String, auditMethod : String, estimatedExposure : BigDecimal) : AuditInformation {
    if (period == null) return null

    var auditInfo = new AuditInformation(
      auditType != null ? auditType : "FinalAudit",
      auditMethod != null ? auditMethod : "Voluntary",
      estimatedExposure != null ? estimatedExposure : period.BasePremium
    )

    auditInfo.setAuditStatus(PCConstants.AUDIT_STATUS_DRAFT)
    var today = LocalDate.now()
    auditInfo.setAuditDueDate(today.plusDays(30).toString())

    return auditInfo
  }

  public static function enterAuditedExposure(auditInfo : AuditInformation, auditedExposure : BigDecimal) : AuditInformation {
    if (auditInfo == null) return null

    auditInfo.setAuditedExposure(auditedExposure != null ? auditedExposure : BigDecimal.ZERO)
    auditInfo.setAuditStatus(PCConstants.AUDIT_STATUS_IN_PROCESS)
    return auditInfo
  }

  public static function calculateAuditAdjustment(auditInfo : AuditInformation, period : PolicyPeriod) : BigDecimal {
    if (auditInfo == null or period == null) return BigDecimal.ZERO

    var adjustment = AuditRatingEngine.calculateAuditPremiumAdjustment(auditInfo, period)
    auditInfo.setAuditPremiumAdjustment(adjustment)
    return adjustment
  }

  public static function closeAudit(auditInfo : AuditInformation, period : PolicyPeriod) : AuditInformation {
    if (auditInfo == null) return null

    calculateAuditAdjustment(auditInfo, period)
    auditInfo.setAuditStatus(PCConstants.AUDIT_STATUS_CLOSED)
    auditInfo.setAuditCompleteDate(LocalDate.now().toString())

    return auditInfo
  }
}
