package features;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import nntest.Util;

import com.sec.gb.ipa.ks.common.util.AsyncCounter;
import com.sec.gb.ipa.ks.common.util.crawler.HtmlHelper;
import com.sec.gb.ipa.ks.common.util.crawler.WebSiteContent;

import qa.pipeline.QAPipelineHelper;
import qa.pipeline.data.SentenceVO;
import qa.pipeline.data.TokenVO;

public class WordFeatureHelper {
	private static final int LAST_NUM = 3;
	private static final int FIRST_NUM = 5;
	private static final int VARIATION_NUM = 3;
	
	
	private static final String SYMBOLS = "abcdefghijklmnopqrstuvwxyz.,!?-:;-/1234567890";
	private static HashMap<Character, Integer> symbolIndex = new HashMap<>();
	
	public enum FeatureType {BINARY_NAME, POS, NER, NAME_FIRST, NAME_LAST, NAME_LEN_UP};
	private static int MAX_LETTERS_IN_WORD = 10;
	private static int LETTERS_IN_ALPHABET = SYMBOLS.length();
	private static int FEATURES_IN_NAME = LETTERS_IN_ALPHABET*MAX_LETTERS_IN_WORD;
	public boolean useClassesIncteadOfBinaryVectors = false;
	private static boolean useLowerCaseText = true;
	
	static final String classNamesFile = "tagList.txt";
	public static HashMap<String, Integer> classesByName = new HashMap<>();
	static HashMap<Integer, String> nameByClass = new HashMap<>();
	
	static Logger logger = Logger.getLogger(WordFeatureHelper.class);
	private ArrayList<SentenceVO> sentences = new ArrayList<>();
	
	static {
		logger.setLevel(Level.INFO);
	}
	
	private static boolean loadTagList() {
		URL url = WordFeatureHelper.class.getClassLoader().getResource(classNamesFile);
		HtmlHelper helper = new HtmlHelper();		
		WebSiteContent wContent = helper.getContent(url, true);
		
		if (wContent == null || wContent.content == null || wContent.content.isEmpty()) {
			return false;
		}
		
		String[] rows = wContent.content.split("\n");
		if(rows.length <= 1) {
			return false;
		}
		
		for (String row : rows) {
			String[] vals = row.split("\t");
			
			int id = Integer.parseInt(vals[0].trim());
			// -1 becouse they named from 1
			classesByName.put(vals[1].trim(), id - 1);
			nameByClass.put(id, vals[1].trim());
		}
		
		return true;
	}
	
	FeatureType[] types;
	
	public WordFeatureHelper(FeatureType[] types) {
		this.types = types;
		
		for (FeatureType featureType : types) {
			initFeature(featureType);
		}
	}
	
	private static boolean initFeature(FeatureType type) {
		switch(type) {
		case BINARY_NAME:
		case NAME_FIRST:
		case NAME_LAST:
		case NAME_LEN_UP:
			
			for(int index = 0; index < LETTERS_IN_ALPHABET; ++ index) {
				symbolIndex.put(SYMBOLS.charAt(index), index);
			}			
			return true;
		case NER:
			return false;
		case POS:
			return loadTagList();
		default:
			return false;
		}
	}
	
	private int getFeatureNumber(FeatureType type) {
		if(!useClassesIncteadOfBinaryVectors) {
			return getClassNumber(type);
		} else {
			switch (type) {
			case BINARY_NAME:
				return MAX_LETTERS_IN_WORD;
			case NER:
				return 0;
			case POS:
				return 1;
			case NAME_FIRST:
				return FIRST_NUM;
			case NAME_LAST:
				return LAST_NUM;
			case NAME_LEN_UP:
				return VARIATION_NUM*(VARIATION_NUM + 1)/2;
			default:
				return 0;
			}
		}
	}

	private static int getClassNumber(FeatureType type) {
		switch (type) {
		case BINARY_NAME:
			return FEATURES_IN_NAME
					//+ MAX_LETTERS_IN_WORD
					;
		case NER:
			return 0;
		case POS:
			return classesByName.size();
		case NAME_FIRST:
			return FIRST_NUM*LETTERS_IN_ALPHABET;
		case NAME_LAST:
			return LAST_NUM*LETTERS_IN_ALPHABET;
		case NAME_LEN_UP:
			return VARIATION_NUM*(VARIATION_NUM + 1)/2*LETTERS_IN_ALPHABET;
		default:
			return 0;
		}
	}
	
	public int getFeatureNumber() {
		int sum = 0;
		for (FeatureType type : types) {
			sum += getFeatureNumber(type);
		}
		
		return sum;
	}
	
	public int getClassNumber() {
		int sum = 0;
		for (FeatureType type : types) {
			sum += getClassNumber(type);
		}
		
		return sum;
	}
	
	private int getFeatures(double[] outArray, int offset, TokenVO token, FeatureType type) {	
		int from = 0;
		int to = MAX_LETTERS_IN_WORD;
		
		switch(type) {
		case NAME_LAST:
			from = MAX_LETTERS_IN_WORD - LAST_NUM;
		case NAME_FIRST:
			if(from == 0) {
				// check need to avoid previous conflict
				to = FIRST_NUM;
			}
		case BINARY_NAME:
			String text = token.getText();
			if(useLowerCaseText) {
				text = text.toLowerCase();
			}
			
			if(!useClassesIncteadOfBinaryVectors) {
				for(int i = from; i < to; ++ i) {
					
					if(i < text.length() && symbolIndex.containsKey(text.charAt(i))) {
						outArray[offset + (i - from)*LETTERS_IN_ALPHABET + symbolIndex.get(text.charAt(i))] = 1.0;
					}
				}
				
				//outArray[offset + FEATURES_IN_NAME + text.length() - 1] = 1;
				
				return (to - from)*LETTERS_IN_ALPHABET;
			} else {
				for(int i = from; i < to; ++ i) {
					if(i < text.length() && symbolIndex.containsKey(text.charAt(i))) {
						outArray[offset + (i - from)] = symbolIndex.get(text.charAt(i));
					}
				}
				return to - from;
			}
		case NER:
			return 0;
		case POS:
			String pos = token.getPos();
			Integer posCode = classesByName.get(pos);
			logger.debug(pos + "->" + posCode);
			
			if(!useClassesIncteadOfBinaryVectors) {	
				
				if(posCode != null) {
					if(classesByName.containsKey(pos)) {
						outArray[offset + posCode] = 1.0;
					}
				} else {
					logger.error("pos code == null. Key = " + pos);
				}
				return classesByName.size();
			} else {
				outArray[offset] = posCode;
				return 1;
			}

		case NAME_LEN_UP:			
			text = token.getText();
			if(useLowerCaseText) {
				text = text.toLowerCase();
			}
			
			int len = text.length();
				
			if(!useClassesIncteadOfBinaryVectors) {				
				for(int i = 0; i < VARIATION_NUM; ++ i) {
					for(int j = 0; j <= i; ++ j) {
						if(len == i + 1 && symbolIndex.containsKey(text.charAt(i))) {
							outArray[offset + symbolIndex.get(text.charAt(i))] = 1.0;
						}
						offset += LETTERS_IN_ALPHABET;
					}
				}
				
				return getFeatureNumber(FeatureType.NAME_LEN_UP);
			} else {
				for(int i = 0; i < VARIATION_NUM; ++ i) {
					for(int j = 0; j <= i; ++ j) {
						if(len == i + 1 && symbolIndex.containsKey(text.charAt(i))) {
							outArray[offset] = symbolIndex.get(text.charAt(i));
						}
						offset += 1;
					}
				}
			}
		default:
			return 0;
		}
	}
	
	public double[][] getFeatures(FeatureType[] types) {
		this.types = types;
		int numFeatures = getFeatureNumber();
				
		ArrayList<double[]> features = new ArrayList<>();
		
		for (SentenceVO sentenceVO : sentences) {
			for(TokenVO token: sentenceVO.getTokens()) {
				double []vector = new double[numFeatures];
				int offset = 0;
				
				//logger.debug(token.getText());
				int offsetTmp = offset;
				//StringBuilder featureBuilder = new StringBuilder();
				
				for(FeatureType type: types) {
					offset += getFeatures(vector, offset, token, type);			
					/*for(int i = offsetTmp; i < offset; ++i) {
						featureBuilder.append(String.format(" %.0f", vector[i]));
					}*/
				}
				//logger.debug(featureBuilder.toString());
				features.add(vector);
				
				String text = token.getText();
				if(text.compareTo(".") == 0 || text.compareTo("?") == 0 || text.compareTo("!") == 0) {
					features.add(null);
				}
			}
		}
		
		double [][] result = new double[features.size()][];
		return features.toArray(result);
	}

	public void analyse(String text) {
		//String[] parts = text.split("(Visitor:)|(Agent:)|(Visitor :)|(Agent :)");
		String[] parts = text.split("\r\n\r\n");
		
		final ArrayList<ArrayList<SentenceVO> > sents = new ArrayList<>();
		int counter = 0;
		final AsyncCounter count = new AsyncCounter();
		ExecutorService service = Executors.newFixedThreadPool(8);
		
		for (String part : parts) {
			sents.add(null);
		}
		
		for (final String string : parts) {
			final int index = counter ++;
			count.inc();
			
			service.execute(new Runnable() {
				
				@Override
				public void run() {
					try {
						sents.set(index, QAPipelineHelper.analyseText(string));
					} catch(Throwable t) {
						logger.error(t.getMessage());
					}
					count.dec();
				}
			});			
		}
		
		count.wait(0);
		service.shutdownNow();
			
		sentences = new ArrayList<>();
		for (ArrayList<SentenceVO> listSents : sents) 
		{
			if(listSents != null) {
				sentences.addAll(listSents);
			}
		}
	}
}
