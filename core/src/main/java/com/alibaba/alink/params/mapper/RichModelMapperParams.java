package com.alibaba.alink.params.mapper;

import com.alibaba.alink.params.shared.colname.HasPredictionCol;
import com.alibaba.alink.params.shared.colname.HasPredictionDetailCol;
import com.alibaba.alink.params.shared.colname.HasReservedColsDefaultAsNull;

/**
 * Params for RichModelMapper.
 */
public interface RichModelMapperParams<T> extends
	ModelMapperParams <T>,
	HasPredictionCol <T>,
	HasPredictionDetailCol <T>,
	HasReservedColsDefaultAsNull <T> {
}
