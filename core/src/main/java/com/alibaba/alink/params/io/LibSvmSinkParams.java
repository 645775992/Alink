package com.alibaba.alink.params.io;

import org.apache.flink.ml.api.misc.param.WithParams;

import com.alibaba.alink.params.shared.HasOverwriteSink;
import com.alibaba.alink.params.shared.colname.HasLabelCol;
import com.alibaba.alink.params.shared.colname.HasVectorCol;

public interface LibSvmSinkParams<T> extends WithParams <T>,
	HasFilePath <T>, HasOverwriteSink <T>, HasVectorCol <T>, HasLabelCol <T>,
	HasStartIndexDefaultAs1 <T> {
}
