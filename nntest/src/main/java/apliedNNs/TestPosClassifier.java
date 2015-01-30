package apliedNNs;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import nntest.BaseNeuralNetwork;
import nntest.BaseNeuroLayer;
import nntest.ClassBasedInputNetwork;
import nntest.FunctionApproximationLayer;
import nntest.NNDynamicClassifierWithDelaysOnInputAndOutput;
import nntest.compute_functions.MaxAggregatorFunction;
import nntest.compute_functions.SigmoidFunction;
import nntest.compute_functions.MaxAggregatorFunction.NormalzeStrategy;
import nntest.test.NeuronTest;

import org.apache.log4j.Logger;

import qa.pipeline.QAPipelineHelper;

import com.sec.gb.ipa.ks.common.util.ResourceHelper;
import com.sec.gb.ipa.ks.common.util.crawler.HtmlHelper;
import com.sec.gb.ipa.ks.common.util.crawler.WebSiteContent;

import features.WordFeatureHelper;
import features.WordFeatureHelper.FeatureType;

public class TestPosClassifier {
	static boolean useClassesInsteadOfBinaryVectors = false;
	
	private static final String TEXT_NAME = "text.txt";
	private static final String TEXT_BIG_NAME = "textBig.txt";
	private static final String TEXT_LIFE_NAME = 
			"textLifeChatSmall";
			//"textLifeChat";

	private static final int HISTORY_SIZE = 1;

	static Logger logger = Logger.getLogger(TestPosClassifier.class);
	static String text = null;
	static String text2 = null;
	
	private static boolean loadText() {
		text = LoadContent(TEXT_NAME);
		text2 = LoadContent(
				//TEXT_NAME
				TEXT_BIG_NAME
				//TEXT_LIFE_NAME
				);
		
		return text != null && text2 != null;
	}

	private static String LoadContent(String fileName) {
		URL url = WordFeatureHelper.class.getClassLoader().getResource(fileName);
		HtmlHelper helper = new HtmlHelper();		
		WebSiteContent wContent = helper.getContent(url, true);
		
		if (wContent == null || wContent.content == null || wContent.content.isEmpty()) {
			return null;
		}
		return wContent.content;
	}
	
	static boolean init() {
		return loadText();
	}
	
	static boolean inited = init(); 
	
	public static void main(String[] args) {
		if(!inited) {
			logger.error("Not inited");
			return;
		}
		
		try {
			QAPipelineHelper.analyseText("");
			FeatureType[] typesInput = new WordFeatureHelper.FeatureType[]{/*WordFeatureHelper.FeatureType.BINARY_NAME,*/WordFeatureHelper.FeatureType.NAME_FIRST 
					/*,WordFeatureHelper.FeatureType.NAME_LAST*/, WordFeatureHelper.FeatureType.NAME_LEN_UP 
					//,WordFeatureHelper.FeatureType.NAME_LEN_UP, WordFeatureHelper.FeatureType.NAME_LEN_UP
					//,WordFeatureHelper.FeatureType.NAME_LEN_UP, WordFeatureHelper.FeatureType.NAME_LEN_UP
					};			
			FeatureType[] typesOutput = new WordFeatureHelper.FeatureType[]{WordFeatureHelper.FeatureType.POS};	
			
			WordFeatureHelper input = new WordFeatureHelper(typesInput);
			WordFeatureHelper output = new WordFeatureHelper(typesOutput);	
			input.useClassesIncteadOfBinaryVectors = useClassesInsteadOfBinaryVectors;
			output.useClassesIncteadOfBinaryVectors = useClassesInsteadOfBinaryVectors;
			
			BaseNeuroLayer classificationLayer = null;
			
			if( !useClassesInsteadOfBinaryVectors ) {
				classificationLayer = new BaseNeuroLayer(output.getFeatureNumber(), HISTORY_SIZE);
				classificationLayer.setComputeFunction(new SigmoidFunction(0.5, 1.0));
			} else {
				classificationLayer = new BaseNeuroLayer(ClassBasedInputNetwork.makeClassificationForest(HISTORY_SIZE, HISTORY_SIZE, output.getClassNumber()));
			}
			
			FunctionApproximationLayer approxLayer = new FunctionApproximationLayer(classificationLayer, new MaxAggregatorFunction(
					NormalzeStrategy.
					EQ_1
					//SOFT_MAX
					//DOMINATING_SOFT_MAX
					));
			
			BaseNeuralNetwork classifier = new NNDynamicClassifierWithDelaysOnInputAndOutput(input.getFeatureNumber(), HISTORY_SIZE, 
					0,
					//0,
					//new boolean[]{true},
					null,
					new ArrayList<>(Arrays.asList(new BaseNeuroLayer[]{classificationLayer, approxLayer})));
			
			
			
			input.analyse(text2);
			output.analyse(text);
			logger.info("Texts parsed");
			double[][] inp = input.getFeatures(typesInput);
			double[][] out = input.getFeatures(typesOutput);
			double[][] inp2 = output.getFeatures(typesInput);
			double[][] out2 = output.getFeatures(typesOutput);
			logger.info("Features computed");
			
			NeuronTest test = new NeuronTest(classifier);
			test.test(inp, out, inp2, out2);
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			return;
		}
	}
	
}
