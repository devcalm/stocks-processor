package org.devcalm.stocks;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConstanceHolder {
    public static final String BATCH_TRACE_ID = "trace-id";
    public static final String BATCH_STOCK_NAME = "stock-name";
    public static final String BATCH_STOCK_START = "stock-start";
    public static final String BATCH_STOCK_END = "stock-end";
    public static final String BATCH_REMOTE_FILE_NAME = "uncompressed-file-name";
    public static final String BATCH_REMOTE_COMPRESSED_FILE = "remote-compressed-uncompressed-file";
    public static final String BATCH_LOCAL_COMPRESSED_FILE = "local-compressed-uncompressed-file";
    public static final String BATCH_LOCAL_UNCOMPRESSED_FILE = "local-uncompressed-uncompressed-file";
}
