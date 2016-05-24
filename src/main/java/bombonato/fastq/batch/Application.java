package bombonato.fastq.batch;

import bombonato.fastq.business.service.FastqSequenceService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.BeansException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import java.io.File;
import java.io.IOException;
import java.util.Date;


@ComponentScan
@EnableAutoConfiguration
public class Application {

	public static final String PARAM_BUFFER_SIZE="buffer=";
	public static final String PARAM_R1="r1=";
	public static final String PARAM_R2="r2=";
	public static final String PARAM_MIN_QUALITY="qual=";
	public static final String PARAM_MIN_QUALITY_PERCENTAGE="qualPerc=";
	public static final String PARAM_MIN_SEQUENCE="seq=";

	public static final String PARAM_HELP="--help";

	public static final String PARAM_SKIP_DUPLICATES="--skip-duplicates";
	public static final String PARAM_SKIP_SIMILAR="--skip-similar";
	public static final String PARAM_SKIP_SIZE="--skip-size";
	public static final String PARAM_SKIP_QUALITY="--skip-quality";
	public static final String PARAM_SKIP_PROGRESS_BAR="--skip-progress-bar";

	public static String datasourceDir = "";

	public static void main(String[] args)
			throws BeansException,
			JobExecutionAlreadyRunningException,
			JobRestartException,
			JobInstanceAlreadyCompleteException,
			JobParametersInvalidException,
			InterruptedException,
			IOException {

		System.out.println("##### FastQ Tasks #####");

		Log log = LogFactory.getLog(Application.class);

		if (args != null && args.length > 0) {

			Date start = new Date();
			Date end = null;

			boolean needHelp = false;

			String r1 = null;
			String r2 = null;
			long minSize = 80L;
			double minQual = 20D;
			double minQualPerc = 80D;

			boolean skipDuplicates = false;
			boolean skipSimilar = false;
			boolean skipSize = false;
			boolean skipLowQuality = false;
			boolean skipProgressBar = false;

			int bufferSize = 100;

			// Prepare application
			for (int i = 0; i < args.length; i++) {
				if (args[i].startsWith(PARAM_HELP)) {
					needHelp = true;

				} else if (args[i].startsWith(PARAM_BUFFER_SIZE)) {
					String buffer[] = args[i].split("=");

					bufferSize = new Integer(buffer[1]);

				} else if (args[i].startsWith(PARAM_R1)) {
					String file[] = args[i].split("=");
					r1 = file[1];

					Application.datasourceDir = Application.generateDatasourceDirectory(r1);
				} else if (args[i].startsWith(PARAM_R2)) {
					String file[] = args[i].split("=");
					r2 = file[1];
				} else if (args[i].startsWith(PARAM_MIN_QUALITY)) {
					String value[] = args[i].split("=");
					minQual = new Double(value[1]);
				} else if (args[i].startsWith(PARAM_MIN_QUALITY_PERCENTAGE)) {
					String value[] = args[i].split("=");
					minQualPerc = new Double(value[1]);
				} else if (args[i].startsWith(PARAM_MIN_SEQUENCE)) {
					String value[] = args[i].split("=");
					minSize = new Long(value[1]);
				} else if (args[i].startsWith(PARAM_SKIP_DUPLICATES)) {
					skipDuplicates = true;
				} else if (args[i].startsWith(PARAM_SKIP_SIMILAR)) {
					skipSimilar = true;
				} else if (args[i].startsWith(PARAM_SKIP_SIZE)) {
					skipSize = true;
				} else if (args[i].startsWith(PARAM_SKIP_QUALITY)) {
					skipLowQuality = true;
				} else if (args[i].startsWith(PARAM_SKIP_PROGRESS_BAR)) {
					skipProgressBar = true;
				}
			}

			if (needHelp) {
				printHelp();
			} else if (r1 == null || "".equals(r1)) {
				printHelp();
			} else {
				// Start Application
				SpringApplication app = new SpringApplication(Application.class);
				app.setWebEnvironment(false);
				app.setShowBanner(false);
				app.setLogStartupInfo(false);

				ConfigurableApplicationContext ctx = app.run(args);

				FastqSequenceService service = ctx.getBean(FastqSequenceService.class);

				System.out.println("Process started: " + start);

				service.sanitize(
						bufferSize,
						r1,
						r2,
						minSize,
						minQual,
						minQualPerc,
						skipDuplicates,
						skipSimilar,
						skipSize,
						skipLowQuality,
						skipProgressBar
				);

				end = new Date();

				System.out.println("Process started: " + start);
				System.out.println("Process finished: " + end);

				Application.removeDatasourceDirectory(Application.datasourceDir);
			}

		} else {
			printHelp();
		}

        System.exit(0);
    }

	private static void printHelp() {
		System.out.println("Usage:");
		System.out.println("  $ java -jar fastq-tasks-0.1.0 r1=/path/to/yourfile.1.fastq --skip-duplicates");

		System.out.println("Options:");
		System.out.println("  Mandatory:");
		System.out.println("    r1          single end fastq or the R1 fastq");
		System.out.println("                file of a paired-end sequencing");
		System.out.println("");
		System.out.println("  Recomended:");
		System.out.println("    r2          the R2 fastq file of a paired-end sequencing");
		System.out.println("");
		System.out.println("  other:");
		System.out.println("    buffer      buffer=SIZE - use this to perform faster processes. Default is 100.");
		System.out.println("    seq         seq=SIZE - minimal size of each sequence. Default is 80.");
		System.out.println("    qual        qual=SCORE - minimal quality of each nucleotide. Default is 20.");
		System.out.println("    qualPerc    qualPerc=PERC - minimal quality percentual of each sequence. Default is 80.");
		System.out.println("");
		System.out.println("    --help              Show this help");
		System.out.println("    --skip-duplicates   Don't process duplicated sequences validation.");
		System.out.println("    --skip-similar      Don't process similar sequences validation.");
		System.out.println("    --skip-size         Don't process size validation.");
		System.out.println("    --skip-quality      Don't process quality validation.");
		System.out.println("    --skip-progress-bar Don't show progress bar in console.");
	}

	private static String generateDatasourceDirectory(String filename) {
		if (filename.contains(".fastq")) {
			filename = filename.replace(".fastq", "_TEMP_DATA");

			File dir = new File(filename);
			if (!dir.exists()) {
				dir.mkdirs();
			}
		}
		return filename;
	}

	private static void removeDatasourceDirectory(String filename) {
		File dir = new File(filename);
		if (dir.exists()) {

			File[] files = dir.listFiles();
			for (File file : files) {
				file.delete();
			}

			dir.delete();
		}
	}

}