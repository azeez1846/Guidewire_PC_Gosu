package gw.pc.batch

import com.guidewire.pc.batch.BatchProcess
import com.guidewire.pc.batch.BatchProcessResult
import com.guidewire.pc.constants.PCConstants
import com.guidewire.pc.model.PolicyPeriod
import com.guidewire.pc.service.DataStoreService
import gw.pc.job.RenewalJobService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

class RenewalBatchProcess implements BatchProcess {

  override public function getType() : String {
    print("[GW-LOG] → RenewalBatchProcess.getType")
    return "GosuRenewalBatch"
  }

  override public function getDescription() : String {
    print("[GW-LOG] → RenewalBatchProcess.getDescription")
    return "Gosu-native Virtual Thread Policy Renewal Batch Process evaluating expiring policies."
  }

  override public function run() : BatchProcessResult {
    print("[GW-LOG] → RenewalBatchProcess.run")
    var dataStore = DataStoreService.getInstance()
    var submissions = dataStore.getSubmissions()
    var renewalsCreated = new AtomicInteger(0)

    try {
      var executor = Executors.newVirtualThreadPerTaskExecutor()
      for (period in submissions) {
        if (PCConstants.STATUS_ISSUED.equalsIgnoreCase(period.Status) and period.PolicyNumber != null) {
          var renewal = RenewalJobService.startRenewal(period)
          if (renewal != null) {
            dataStore.createSubmission(renewal)
            renewalsCreated.incrementAndGet()
          }
        }
      }
      executor.close()
    } catch (e : Exception) {
      return new BatchProcessResult(getType(), false, renewalsCreated.get(), "Gosu Renewal Batch failed: " + e.Message)
    }

    return new BatchProcessResult(getType(), true, renewalsCreated.get(), "Gosu Renewal Batch processed " + renewalsCreated.get() + " policies successfully.")
  }
}
