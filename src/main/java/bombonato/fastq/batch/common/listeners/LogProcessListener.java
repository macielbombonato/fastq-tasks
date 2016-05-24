package bombonato.fastq.batch.common.listeners;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.ItemProcessListener;

public class LogProcessListener implements ItemProcessListener<Object, Object> {

	private static final Log log = LogFactory.getLog(LogProcessListener.class);

	public void afterProcess(Object item, Object result) {
		if(item!=null) log.debug("Input to Processor: " + item.toString());
		if(result!=null) log.debug("Output of Processor: " + result.toString());
	}

	public void beforeProcess(Object item) {
		log.debug(item.toString());
	}

	public void onProcessError(Object item, Exception e) {
		log.error(item.toString());
		log.error(e);
	}

}
