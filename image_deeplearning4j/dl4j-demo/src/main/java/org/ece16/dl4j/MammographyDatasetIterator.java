package org.ece16.dl4j;

import org.deeplearning4j.datasets.iterator.BaseDatasetIterator;

public class MammographyDatasetIterator  extends BaseDatasetIterator  {
	
	private static final long serialVersionUID = 1L;

	public MammographyDatasetIterator(int batchSize, MammographyDataFetcher dataFetcher) {
		super(batchSize, dataFetcher.totalExamples(), dataFetcher);
	}
}
