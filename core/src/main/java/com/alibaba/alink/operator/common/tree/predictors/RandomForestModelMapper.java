package com.alibaba.alink.operator.common.tree.predictors;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.ml.api.misc.param.Params;
import org.apache.flink.table.api.TableSchema;
import org.apache.flink.types.Row;

import com.alibaba.alink.common.utils.JsonConverter;
import com.alibaba.alink.operator.common.tree.Criteria;
import com.alibaba.alink.operator.common.tree.LabelCounter;
import com.alibaba.alink.operator.common.tree.Node;
import com.alibaba.alink.operator.common.tree.TreeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RandomForestModelMapper extends TreeModelMapper {
	private static final Logger LOG = LoggerFactory.getLogger(RandomForestModelMapper.class);
	private static final long serialVersionUID = 1392112308487523143L;

	private transient ThreadLocal<Row> inputBufferThreadLocal;

	public RandomForestModelMapper(
		TableSchema modelSchema,
		TableSchema dataSchema,
		Params params) {
		super(modelSchema, dataSchema, params);
	}

	@Override
	public void loadModel(List <Row> modelRows) {
		init(modelRows);

		inputBufferThreadLocal = ThreadLocal.withInitial(() -> new Row(ioSchema.f0.length));
	}

	@Override
	protected Tuple2 <Object, String> predictResultDetail(SlicedSelectedSample selection) throws Exception {
		Node[] root = treeModel.roots;

		Row inputBuffer = inputBufferThreadLocal.get();

		selection.fillRow(inputBuffer);

		transform(inputBuffer);

		int len = root.length;

		Object result = null;
		Map <String, Double> detail = null;

		if (len > 0) {
			LabelCounter labelCounter = new LabelCounter(
				0, 0, new double[root[0].getCounter().getDistributions().length]);

			predict(inputBuffer, root[0], labelCounter, 1.0);

			for (int i = 1; i < len; ++i) {
				predict(inputBuffer, root[i], labelCounter, 1.0);
			}

			labelCounter.normWithWeight();

			if (!Criteria.isRegression(treeModel.meta.get(TreeUtil.TREE_TYPE))) {
				detail = new HashMap <>();
				double[] probability = labelCounter.getDistributions();
				double max = 0.0;
				int maxIndex = -1;
				for (int i = 0; i < probability.length; ++i) {
					detail.put(String.valueOf(treeModel.labels[i]), probability[i]);
					if (max < probability[i]) {
						max = probability[i];
						maxIndex = i;
					}
				}

				if (maxIndex == -1) {
					LOG.warn("Can not find the probability: {}", JsonConverter.toJson(probability));
				}

				result = treeModel.labels[maxIndex];
			} else {
				result = labelCounter.getDistributions()[0];
			}
		}

		return new Tuple2 <>(result, detail == null ? null : JsonConverter.toJson(detail));
	}

	@Override
	protected Object predictResult(SlicedSelectedSample selection) throws Exception {
		return predictResultDetail(selection).f0;
	}
}
