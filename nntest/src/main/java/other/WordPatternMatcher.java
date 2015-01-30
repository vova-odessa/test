package other;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sec.gb.ipa.ks.common.util.ResourceHelper;

import qa.pipeline.QAPipelineHelper;
import qa.pipeline.data.SentenceVO;
import qa.pipeline.data.TokenVO;

public class WordPatternMatcher implements IWordProcessor, IPatternListener {
	private static Logger logger = LoggerFactory.getLogger(WordPatternMatcher.class);
	
	private static HashSet<String> stopPhraseWords = new HashSet<>(Arrays.asList(new String[] {".", ",", ";", "...", "!", "?", ":", "\"", "'", "-"}));
	
	HashSet<Pattern> allPatterns = new HashSet<>();
	HashMap<Pattern, Long> activatedPatterns = new HashMap<>();

	HashMap<String, HashSet<Pattern> > patterns = new HashMap<>();
	HashSet<IWordProcessor> matchers = new HashSet<>();
	HashSet<IWordProcessor> catdidatesToRemove = new HashSet<>();
	HashSet<String> currentCategories = new HashSet<>();
	HashSet<String> currentCategoriesCandidates = new HashSet<>();
	Word lastWord = null;
	
	public void addPattern(Pattern pattern) {
		if(allPatterns.contains(pattern)) {
			//logger.info("addPattern: pattern already exist");
			return ;
		}
		
		if(pattern != null) {
			String patternStartElement = pattern.start();
			
			if(!patterns.containsKey(patternStartElement)) {
				patterns.put(patternStartElement, new HashSet<Pattern>());
			}
			patterns.get(patternStartElement).add(pattern);
			
			allPatterns.add(pattern);
		}
	}
	
	public void addPattern(String[] words, String[] categories) {
		addPattern(words, categories, null);
	}
	
	public void addPattern(String[] words, String[] categories, String category) {
		Pattern pattern = new Pattern();
		pattern.assign(words, categories);
		if(category != null) {
			pattern.assignCategory(category);
		}
		addPattern(pattern);
	}

	/**
	 * IWordProcessor
	 */
	@Override
	public void addNextWord(Word word) {
		// stop listen updates from last words. Because new pattern matchers can not be start from newly found phrases from past.
		if(lastWord != null) {
			lastWord.stopListen(this);
		}
		
		// start listen for current word ipdates
		currentCategories = new HashSet<>();
		word.listenCategories(this);
		lastWord = word;
		
		// track already started matchers to get more categories
		for(IWordProcessor matcher: matchers) {
			matcher.addNextWord(word);
		}
	
		// remove finished matchers at the moment
		removeFinishedMatchers();
		
		// check new possible matchers that can be started by just word
		startMatchersByWord(word.word, word);
		
		if(word.getCategories().size() > 0) {
			for (Entry<String, Phrase> category : word.getCategories().entrySet()) {
				if(category.getValue().words.size() == 1) {
					startMatchersByWord("<" + category.getKey() + ">", word);
				}
			}
		}
		
		while(currentCategoriesCandidates.size() > 0) { 
			// this procedure is recursivelly until not more new categories
			currentCategories.addAll(currentCategoriesCandidates);
			HashSet<String> candidatesCopy = currentCategoriesCandidates;
			currentCategoriesCandidates = new HashSet<>();
			
			for (String category : candidatesCopy) {
				// check for new "category-based" matchers
				startMatchersByWord("<" + category + ">", word);
			}
		}
	}

	private void startMatchersByWord(String text, Word word) {
		HashSet<Pattern> currPatterns = new HashSet<>();
		
		if(patterns.containsKey(text)) {
			currPatterns.addAll(patterns.get(text));			
		}

		if(patterns.get("<>") != null) {
			currPatterns.addAll(patterns.get("<>"));
		}
		
		if(patterns.get("<*>") != null) {
			currPatterns.addAll(patterns.get("<*>"));
		}
		
		if(currPatterns != null) {
			for (Pattern pattern : currPatterns) {
				SinglePatternSingleMatcher matcher = new SinglePatternSingleMatcher(pattern);
				matcher.listen(this);
				matchers.add(matcher);
				matcher.addNextWord(word);
			}
			
			removeFinishedMatchers();
		}
	}

	private void removeFinishedMatchers() {
		matchers.removeAll(catdidatesToRemove);
		catdidatesToRemove = new HashSet<>();
	} 

	@Override
	public void clearWordContext() {
		finishMultiwordPhrases();
		matchers = new HashSet<>();
	}

	@Override
	public void clearCategoryContext() {
		finishMultiwordPhrases();
		matchers = new HashSet<>();
	}

	@Override
	public void updatedCategoryEnd(Word word, String category, Phrase phrase) {
		if(!currentCategories.contains(category)) {
			currentCategoriesCandidates.add(category);
		}
	}

	@Override
	public void updatedCategoryStart(Word word, String category, Phrase phrase) {
		if(!currentCategories.contains(category)) {
			currentCategoriesCandidates.add(category);
		}
	}
	
	/**
	 * end IWordProcessor
	 */

	/**
	 * IPatternListener
	 */
	@Override
	public void pastPattern(IWordProcessor processor, Phrase phrase, String category) {
		notifyPattern(phrase, category);
	}

	@Override
	public void currentPattern(IWordProcessor processor, Phrase phrase,	String category) {
		notifyPattern(phrase, category);
	}

	@Override
	public void activated(IWordProcessor processor) {
		catdidatesToRemove.add(processor);
	}

	@Override
	public void rejected(IWordProcessor processor) {
		catdidatesToRemove.add(processor);
	}

	@Override
	public void wordPassed(IWordProcessor processor) {
		// XXX		
	}
	
	/**
	 * end IPatternListener
	 */
	
	private void notifyPattern(Phrase phrase, String category) {
		Word end = phrase.end();
		if(end != null) {
			end.addCategory(phrase, category);
		} else {
			logger.error("currentPattern: phrase is empty");
		}
		Word start = phrase.start();
		if(start != null) {
			start.addCategory(phrase, category);
		} else {
			logger.error("currentPattern: phrase start is empty");
		}
		
		// count pattern
		Pattern pattern = new Pattern();
		pattern.addPhrase(phrase.toString(), category);
		pattern.assignCategory(category);
		
		addPattern(pattern);		
		if(!activatedPatterns.containsKey(pattern)) {
			activatedPatterns.put(pattern, (long) 0);
		}
		
		activatedPatterns.put(pattern, activatedPatterns.get(pattern) + 1);
	}
	
	public void LogStats() {
		StringBuilder builder = new StringBuilder();
		for (Entry<Pattern, Long> entry : activatedPatterns.entrySet()) {
			builder.append("\n");
			builder.append(entry.getValue() + ": " + entry.getKey().toString());
		}
		
		logger.info(builder.toString());
	}
	
	public void processText(String text) {
		ArrayList<SentenceVO> sentences = QAPipelineHelper.analyseText(text);
		for (SentenceVO sentence : sentences) {
			clearWordContext();
			int id = 0;
			for(TokenVO token: sentence.getTokens()) {
				Word word = new Word(token.getText(), ++id);
				Phrase self = new Phrase();
				self.add(word);
				
				if(token.getCategory() != null && !token.getCategory().isEmpty()) {
					word.addCategory(self, token.getCategory());
				}
				
				if(stopPhraseWords.contains(word.word)) {
					finishMultiwordPhrases();
				}
				addNextWord(word);
			}
		}
	}
	
	public static void main(String[] args) {
		WordPatternMatcher matcher = new WordPatternMatcher();
		
		//matcher.addPattern(new String[]{"it", "is", "<>"}, new String[] {"subjective", "verb", "objective"});
		//matcher.addPattern(new String[]{"it", "is", "<>"}, new String[] {"", "", "objective"});		
		matcher.addPattern(new String[]{"it", "is", "the", "<>"}, new String[] {"", "", null, "objective"});		
		matcher.addPattern(new String[]{"it", "is", "the", "<objective> <>"}, new String[] {"", "", null, "o2"});	
		matcher.addPattern(new String[]{"it", "is", "the", "<o2> <>"}, new String[] {"", "", null, "o3"});
		matcher.addPattern(new String[]{"it", "is", "the", "<o3> <>"}, new String[] {"", "", null, "o4"});
		matcher.addPattern(new String[]{"it", "is", "the", "<*>"}, new String[] {"", "", null, "o5"});
		//matcher.addPattern(new String[]{"<>", "<date>"}, new String[] {null, null}, "dt");
		//matcher.addPattern(new String[]{"<*>","<date>"}, new String[] {null, null}, "dt1");
		
		
		
		//matcher.processText(ResourceHelper.loadResource("textLifeChatSmall").content);
		matcher.processText(ResourceHelper.loadResource("textLifeChatSmall2").content);
		//matcher.processText(ResourceHelper.loadResource("textTest.txt").content);
		matcher.LogStats();
		
		
	}

	@Override
	public boolean finishMultiwordPhrases() {
		for (IWordProcessor matcher : matchers) {
			matcher.finishMultiwordPhrases();
			if(matcher.finishMultiwordPhrases()) {
				catdidatesToRemove.add(matcher);
			}
		}
		
		removeFinishedMatchers();
		
		return false;
	}

	@Override
	public boolean isFinished() {
		return false;
	}
}
