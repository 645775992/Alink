package com.alibaba.alink.operator.common.regression;

import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.ml.api.misc.param.Params;
import org.apache.flink.table.api.TableSchema;
import org.apache.flink.types.Row;

import com.alibaba.alink.common.linalg.VectorUtil;
import com.alibaba.alink.common.mapper.ModelMapper;
import com.alibaba.alink.common.utils.RowUtil;
import com.alibaba.alink.common.utils.TableUtil;
import com.alibaba.alink.operator.common.dataproc.ScalerUtil;
import com.alibaba.alink.params.regression.IsotonicRegPredictParams;
import com.alibaba.alink.params.regression.IsotonicRegTrainParams;

import java.util.Arrays;
import java.util.List;

/**
 * This mapper predicts the isotonic regression result.
 */
public class IsotonicRegressionModelMapper extends ModelMapper {
	private static final long serialVersionUID = 4565470971830328037L;
	private int colIdx;
	private IsotonicRegressionModelData modelData;
	private String vectorColName;
	private int featureIndex;

	/**
	 * Constructor.
	 *
	 * @param modelSchema the model schema.
	 * @param dataSchema  the data schema.
	 * @param params      the params.
	 */
	public IsotonicRegressionModelMapper(TableSchema modelSchema, TableSchema dataSchema, Params params) {
		super(modelSchema, dataSchema, params);
	}

	/**
	 * Load model from the list of Row type data.
	 *
	 * @param modelRows the list of Row type data.
	 */
	@Override
	public void loadModel(List <Row> modelRows) {
		IsotonicRegressionConverter converter = new IsotonicRegressionConverter();
		modelData = converter.load(modelRows);
		Params meta = modelData.meta;
		String featureColName = meta.get(IsotonicRegTrainParams.FEATURE_COL);
		vectorColName = meta.get(IsotonicRegTrainParams.VECTOR_COL);
		featureIndex = meta.get(IsotonicRegTrainParams.FEATURE_INDEX);
		TableSchema dataSchema = getDataSchema();
		if (null == vectorColName) {
			colIdx = TableUtil.findColIndexWithAssert(dataSchema.getFieldNames(), featureColName);
		} else {
			colIdx = TableUtil.findColIndexWithAssert(dataSchema.getFieldNames(), vectorColName);
		}
	}



	@Override
	protected Tuple4 <String[], String[], TypeInformation <?>[], String[]> prepareIoSchema(TableSchema modelSchema,
																						   TableSchema dataSchema,
																						   Params params) {
		String[] selectedCols = dataSchema.getFieldNames();
		String[] resCols = new String[] {this.params.get(IsotonicRegPredictParams.PREDICTION_COL)};
		TypeInformation[] resTypes = new TypeInformation[] {Types.DOUBLE};
		return Tuple4.of(selectedCols, resCols, resTypes, null);
	}

	/**
	 * Map operation method.
	 *
	 * @throws Exception This method may throw exceptions. Throwing
	 * an exception will cause the operation to fail.
	 */
	@Override
	protected void map(SlicedSelectedSample selection, SlicedResult result) throws Exception {
		if (null == selection.get(colIdx)) {
			result.set(0, null);
			return;
		}
		//use Binary Search method to search for the boundaries.
		double feature = (null == this.vectorColName ? ((Number) selection.get(colIdx)).doubleValue() :
			VectorUtil.getVector(selection.get(colIdx)).get(this.featureIndex));
		int foundIndex = Arrays.binarySearch(modelData.boundaries, feature);
		int insertIndex = -foundIndex - 1;
		double predict;
		if (insertIndex == 0) {
			predict = modelData.values[0];
		} else if (insertIndex == modelData.boundaries.length) {
			predict = modelData.values[modelData.values.length - 1];
		} else if (foundIndex < 0) {
			predict = ScalerUtil.minMaxScaler(feature, modelData.boundaries[insertIndex - 1],
				modelData.boundaries[insertIndex],
				modelData.values[insertIndex], modelData.values[insertIndex - 1]);
		} else {
			predict = modelData.values[foundIndex];
		}
		result.set(0, predict);
		return;
	}

}
