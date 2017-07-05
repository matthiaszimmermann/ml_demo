package org.ece16.dl4j.wdbc;

import org.deeplearning4j.datasets.iterator.BaseDatasetIterator;

public class WDBCDatasetIterator  extends BaseDatasetIterator  {
	
	private static final long serialVersionUID = 1L;

	public WDBCDatasetIterator(int batchSize, WDBCDataFetcher dataFetcher) {
		super(batchSize, dataFetcher.totalExamples(), dataFetcher);
	}
}
