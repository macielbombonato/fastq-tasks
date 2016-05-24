package bombonato.fastq.data.enums;

import java.util.HashMap;
import java.util.Map;

public enum Status {

    USE("use") {
        @Override
        public String getExtension() {
            return ".RECODE.fastq";
        }
    },
    SMALL("small") {
        @Override
        public String getExtension() {
            return ".SMALL-SEQ.fastq";
        }
    },
    LOW_QUALITY("low_quality") {
        @Override
        public String getExtension() {
            return ".LOW-QUALITY.fastq";
        }
    },
    DUPLICATE("duplicate") {
        @Override
        public String getExtension() {
            return ".DUPLICATE.fastq";
        }
    },
    SIMILAR("similar") {
        @Override
        public String getExtension() {
            return ".SIMILAR.fastq";
        }
    },

    ;

    private final String code;

    private static final Map<String, Status> valueMap;

    static {
        valueMap = new HashMap<String, Status>();
        for (Status type : values()) {
            valueMap.put(type.code, type);
        }
    }

    public static Status fromCode(String code) {
        return valueMap.get(code);
    }

    private Status(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public abstract String getExtension();
}
