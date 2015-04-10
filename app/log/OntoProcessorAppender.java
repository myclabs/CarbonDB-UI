package log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;


public class OntoProcessorAppender extends AppenderBase<ILoggingEvent> {

    public void append(ILoggingEvent event) {
        Level level = event.getLevel();
        if (Level.WARN_INT == level.levelInt) {
            OntoProcessorMessageStore.getInstance().addWarning(event.getMessage());
        }
        else if (Level.ERROR_INT == level.levelInt) {
            OntoProcessorMessageStore.getInstance().addError(event.getMessage());
        }
    }
}