package com.guidewire.pc.service;

import com.guidewire.pc.model.PolicyPeriod;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import java.util.logging.Level;

public class ParallelAcceleratorService {
    private static final Logger LOGGER = Logger.getLogger(ParallelAcceleratorService.class.getName());
    private static final ParallelAcceleratorService instance = new ParallelAcceleratorService();

    private final VinLookupService vinLookupService = VinLookupService.getInstance();
    private final RatingEngine ratingEngine = RatingEngine.getInstance();
    private final AIUnderwritingAssistant aiAssistant = AIUnderwritingAssistant.getInstance();

    private ParallelAcceleratorService() {
        LOGGER.log(Level.FINE, "→ ParallelAcceleratorService.ParallelAcceleratorService");}

    public static ParallelAcceleratorService getInstance() {
        LOGGER.log(Level.FINE, "→ ParallelAcceleratorService.getInstance");
        return instance;
    }

    public static class ParallelEvaluationResult {
        private long executionTimeMs;
        private Object vinResult;
        private Object ratingResult;
        private Object aiUnderwritingResult;
        private final Map<String, Object> extraResults = new HashMap<>();

        public long getExecutionTimeMs() {
        LOGGER.log(Level.FINE, "→ ParallelAcceleratorService.getExecutionTimeMs"); return executionTimeMs; }
        public void setExecutionTimeMs(long executionTimeMs) {
        LOGGER.log(Level.FINE, "→ ParallelAcceleratorService.setExecutionTimeMs"); this.executionTimeMs = executionTimeMs; }
        public Object getVinResult() {
        LOGGER.log(Level.FINE, "→ ParallelAcceleratorService.getVinResult"); return vinResult; }
        public void setVinResult(Object vinResult) {
        LOGGER.log(Level.FINE, "→ ParallelAcceleratorService.setVinResult"); this.vinResult = vinResult; }
        public Object getRatingResult() {
        LOGGER.log(Level.FINE, "→ ParallelAcceleratorService.getRatingResult"); return ratingResult; }
        public void setRatingResult(Object ratingResult) {
        LOGGER.log(Level.FINE, "→ ParallelAcceleratorService.setRatingResult"); this.ratingResult = ratingResult; }
        public Object getAiUnderwritingResult() {
        LOGGER.log(Level.FINE, "→ ParallelAcceleratorService.getAiUnderwritingResult"); return aiUnderwritingResult; }
        public void setAiUnderwritingResult(Object aiUnderwritingResult) {
        LOGGER.log(Level.FINE, "→ ParallelAcceleratorService.setAiUnderwritingResult"); this.aiUnderwritingResult = aiUnderwritingResult; }
        public Map<String, Object> getExtraResults() {
        LOGGER.log(Level.FINE, "→ ParallelAcceleratorService.getExtraResults"); return extraResults; }
    }

    public ParallelEvaluationResult evaluatePolicyAcceleratorsInParallel(String vin, PolicyPeriod period) {
        LOGGER.log(Level.FINE, "→ ParallelAcceleratorService.evaluatePolicyAcceleratorsInParallel");
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
