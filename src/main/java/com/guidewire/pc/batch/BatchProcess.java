package com.guidewire.pc.batch;
import java.util.logging.Logger;
import java.util.logging.Level;

public interface BatchProcess {
    String getType();
    String getDescription();
    BatchProcessResult run();
}
