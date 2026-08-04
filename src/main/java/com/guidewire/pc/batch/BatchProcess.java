package com.guidewire.pc.batch;

public interface BatchProcess {
    String getType();
    String getDescription();
    BatchProcessResult run();
}
