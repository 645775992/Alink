# 逻辑回归训练 (LogisticRegressionTrainBatchOp)
Java 类名：com.alibaba.alink.operator.batch.classification.LogisticRegressionTrainBatchOp

Python 类名：LogisticRegressionTrainBatchOp


## 功能介绍
逻辑回归算法是经典的二分类算法，通过对打标签样本集合训练得到模型，使用模型预测样本的标签。逻辑回归组件支持稀疏、稠密两种数据格式。

### 算法原理
面对二分类问题，建立代价函数，然后通过优化方法迭代求解出最优的模型参数，然后测试验证我们这个求解的模型的好坏。Logistic回归虽然名字里带“回归”，
但是它实际上是一种分类方法，主要用于二分类问题（即输出只有两种，分别代表两个类别）回归模型中，y是一个定性变量，比如y=0或1，
logistic方法主要应用于研究某些事件发生的概率。

### 算法使用
常用于数据挖掘，疾病自动诊断，经济预测等领域。例如，探讨引发疾病的危险因素，并根据危险因素预测疾病发生的概率等。以心脏病病情分析为例，选择两组人群，
一组是心脏病组，一组是非心脏病组，两组人群必定具有不同的属性及身体指标。因此因变量就为是否有心脏病，值为“是”或“否”，自变量就可以包括很多了，
如年龄、性别、最大心跳数、血压、胆固醇、空腹血糖等。自变量既可以是连续的，也可以是分类的。然后通过logistic回归分析，可以得到自变量的权重，
从而可以大致了解到底哪些因素是心脏病的危险因素。同时根据该权值可以根据危险因素预测一个人心脏病的可能性。

- 备注 ：该组件训练的时候 FeatureCols 和 VectorCol 是两个互斥参数，只能有一个参数来描述算法的输入特征。

### 文献或出处
[1] Wright, R. E. (1995). Logistic regression. In L. G. Grimm & P. R. Yarnold (Eds.), Reading and understanding multivariate statistics (pp. 217–244). American Psychological Association.

[2] https://baike.baidu.com/item/logistic%E5%9B%9E%E5%BD%92

## 参数说明


| 名称 | 中文名称 | 描述 | 类型 | 是否必须？ | 取值范围 | 默认值 |
| --- | --- | --- | --- | --- | --- | --- |
| labelCol | 标签列名 | 输入表中的标签列名 | String | ✓ |  |  |
| epsilon | 收敛阈值 | 迭代方法的终止判断阈值，默认值为 1.0e-6 | Double |  | [0.0, +inf) | 1.0E-6 |
| featureCols | 特征列名数组 | 特征列名数组，默认全选 | String[] |  | 所选列类型为 [BIGDECIMAL, BIGINTEGER, BYTE, DOUBLE, FLOAT, INTEGER, LONG, SHORT] | null |
| l1 | L1 正则化系数 | L1 正则化系数，默认为0。 | Double |  | [0.0, +inf) | 0.0 |
| l2 | 正则化系数 | L2 正则化系数，默认为0。 | Double |  | [0.0, +inf) | 0.0 |
| maxIter | 最大迭代步数 | 最大迭代步数，默认为 100 | Integer |  | [1, +inf) | 100 |
| optimMethod | 优化方法 | 优化问题求解时选择的优化方法 | String |  | "LBFGS", "GD", "Newton", "SGD", "OWLQN" | null |
| standardization | 是否正则化 | 是否对训练数据做正则化，默认true | Boolean |  |  | true |
| vectorCol | 向量列名 | 向量列对应的列名，默认值是null | String |  | 所选列类型为 [DENSE_VECTOR, SPARSE_VECTOR, STRING, VECTOR] | null |
| weightCol | 权重列名 | 权重列对应的列名 | String |  | 所选列类型为 [BIGDECIMAL, BIGINTEGER, BYTE, DOUBLE, FLOAT, INTEGER, LONG, SHORT] | null |
| withIntercept | 是否有常数项 | 是否有常数项，默认true | Boolean |  |  | true |



## 代码示例
### Python 代码
```python
from pyalink.alink import *

import pandas as pd

useLocalEnv(1)

df_data = pd.DataFrame([
    [2, 1, 1],
    [3, 2, 1],
    [4, 3, 2],
    [2, 4, 1],
    [2, 2, 1],
    [4, 3, 2],
    [1, 2, 1],
    [5, 3, 2]
])

input = BatchOperator.fromDataframe(df_data, schemaStr='f0 int, f1 int, label int')

# load data
dataTest = input
colnames = ["f0","f1"]
lr = LogisticRegressionTrainBatchOp().setFeatureCols(colnames).setLabelCol("label")
model = input.link(lr)

predictor = LogisticRegressionPredictBatchOp().setPredictionCol("pred")
predictor.linkFrom(model, dataTest).print()
```
### Java 代码
```java
import org.apache.flink.types.Row;

import com.alibaba.alink.operator.batch.BatchOperator;
import com.alibaba.alink.operator.batch.classification.LogisticRegressionPredictBatchOp;
import com.alibaba.alink.operator.batch.classification.LogisticRegressionTrainBatchOp;
import com.alibaba.alink.operator.batch.source.MemSourceBatchOp;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class LogisticRegressionTrainBatchOpTest {
	@Test
	public void testLogisticRegressionTrainBatchOp() throws Exception {
		List <Row> df_data = Arrays.asList(
			Row.of(2, 1, 1),
			Row.of(3, 2, 1),
			Row.of(4, 3, 2),
			Row.of(2, 4, 1),
			Row.of(2, 2, 1),
			Row.of(4, 3, 2),
			Row.of(1, 2, 1),
			Row.of(5, 3, 2)
		);
		BatchOperator <?> input = new MemSourceBatchOp(df_data, "f0 int, f1 int, label int");
		BatchOperator dataTest = input;
		BatchOperator <?> lr = new LogisticRegressionTrainBatchOp().setFeatureCols("f0", "f1").setLabelCol("label");
		BatchOperator model = input.link(lr);
		BatchOperator <?> predictor = new LogisticRegressionPredictBatchOp().setPredictionCol("pred");
		predictor.linkFrom(model, dataTest).print();
	}
}
```
### 运行结果
f0 | f1 | label | pred 
---|----|-------|-----
2|1|1|1
3|2|1|1
4|3|2|2
2|4|1|1
2|2|1|1
4|3|2|2
1|2|1|1
5|3|2|2



## 备注

1. 该组件的输入为训练数据，输出为逻辑回归模型。
2. 参数数据库的使用方式可以覆盖多个参数的使用方式。

