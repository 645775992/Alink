package com.alibaba.alink.params.regression;

import com.alibaba.alink.params.mapper.ModelMapperParams;
import com.alibaba.alink.params.shared.colname.HasPredictionCol;
import com.alibaba.alink.params.shared.colname.HasReservedColsDefaultAsNull;

public interface RegPredictParams<T> extends
	ModelMapperParams <T>,
	HasPredictionCol <T>,
	HasReservedColsDefaultAsNull <T> {
}
