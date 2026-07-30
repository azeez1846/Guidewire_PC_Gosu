package com.guidewire.pc.service;

import com.guidewire.pc.model.PolicyPeriod;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class ParallelAcceleratorService {
    private static final Logger LOGGER = Logger.getLogger(ParallelAcceleratorService.class.getName());
    private static final ParallelAcceleratorService instance = new ParallelAcceleratorService();

    private final VinLookupService vinLookupService = VinLookupService.getInstance();
    private final RatingEngine ratingEngine = RatingEngine.getInstance();
    private final AIUnderwritingAssistant aiAssistant = AIUnderwritingAssistant.getInstance();

    private ParallelAcceleratorService() {}

    public static ParallelAcceleratorService getInstance() {
        return instance;
    }

    public static class ParallelEvaluationResult {
        private long executionTimeMs;
        private Object vinResult;
        private Object ratingResult;
        private Object aiUnderwritingResult;
        private final Map<String, Object> extraResults = new HashMap<>();

        public long getExecutionTimeMs() { return executionTimeMs; }
        public void setExecutionTimeMs(long executionTimeMs) { this.executionTimeMs = executionTimeMs; }
        public Object getVinResult() { return vinResult; }
        public void setVinResult(Object vinResult) { this.vinResult = vinResult; }
        public Object getRatingResult() { return ratingResult; }
        public void setRatingResult(Object ratingResult) { this.ratingResult = ratingResult; }
        public Object getAiUnderwritingResult() { return aiUnderwritingResult; }
        public void setAiUnderwritingResult(Object aiUnderwritingResult) { this.aiUnderwritingResult = aiUnderwritingResult; }
        public Map<String, Object> getExtraResults() { return extraResults; }
    }

    public ParallelEvaluationResult evaluatePolicyAcceleratorsInParallel(String vin, PolicyPeriod period) {
        long startTime = System.currentTimeMillis();
        ParallelEvaluationResult result = new ParallelEvaluationResult();

        try (ExecutorService virtualExecutor = Executors.newVirtualThreadPerTaskExecutor()) {
            CompletableFuture<Object> vinFuture = CompletableFuture.supplyAsync(() -> {
                if (vin != null && !vin.trim().isEmpty()) {
                    return vinLookupService.decodeVin(vin);
                }
                return "No VIN provided";
            }, virtualExecutor);

            CompletableFuture<Object> ratingFuture = CompletableFuture.supplyAsync(() -> {
                if (period != null) {
                    return ratingEngine.rate(period);
                }
                return "No PolicyPeriod provided";
            }, virtualExecutor);

            CompletableFuture<Object> aiFuture = CompletableFuture.supplyAsync(() -> {
                if (period != null) {
                    return aiAssistant.triageSubmission(period, 0.0, 720);
                }
                return "No PolicyPeriod provided";
            }, virtualExecutor);

            CompletableFuture.allOf(vinFuture, ratingFuture, aiFuture).join();

            result.setVinResult(vinFuture.get());
            result.setRatingResult(ratingFuture.get());
            result.setAiUnderwritingResult(aiFuture.get());
        } catch (Exception e) {
            LOGGER.log(java.util.logging.Level.SEVERE, "Parallel accelerator evaluation error: {0}", e.getMessage());
        }

        long endTime = System.currentTimeMillis();
        result.setExecutionTimeMs(endTime - startTime);
        return result;
    }
}
