package com.mwdb.phase2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mwdb.phase1.PhaseOne;
import com.mwdb.phase1.Word;
import com.mwdb.util.PrecomputeUtil;

public class AllComponents {
	public Map<String, Map<Integer, Map<String, Word>>> precomputedMap = new HashMap<String, Map<Integer,Map<String,Word>>>();
	Map<String, Double> vocabulary = new HashMap<String, Double>();
	Map<String, Double> dfMap = new HashMap<String, Double>();
	public Map<String, Double> queryVocabulary = new HashMap<String, Double>();
	public Map<String, Double> queryVocabulary2 = new HashMap<String, Double>();
	public Map<String, Double> queryVocabulary1 = new HashMap<String, Double>();
	public Map<String, Map<String, Double>> vocabulary2 = new HashMap<String, Map<String, Double>>();
	public Map<String, Map<Integer, Map<String, Word>>> queryMap = new HashMap<String, Map<Integer, Map<String, Word>>>();
	public Map<String, Map<Integer, Map<Integer, Double>>> precomputedLDATopicMap;
	public Map<String, Map<Integer, Map<Integer, Double>>> precomputedTopicMap = new HashMap<String, Map<Integer, Map<Integer, Double>>>();
	public Map<String, Map<Integer, Map<Integer, Double>>> precomputedPCATopicMap = new HashMap<String, Map<Integer, Map<Integer, Double>>>();
	public Map<String, Map<Integer, Map<Integer, Double>>> precomputedTopicQueryMap = new HashMap<String, Map<Integer, Map<Integer, Double>>>();
	public String folderName;
	public Map<Integer, Map<Integer, Map<String, Double>>> finalSVDMap = new HashMap<Integer, Map<Integer, Map<String, Double>>>();

	private PrecomputeUtil precomputeUtil;
	public  PhaseOne phaseOne;
	public  int WINDOW_LENGTH;
	public  int SHIFT_LENGTH;
	int r;
	public  double NUMBER_OF_OBJECTS;
	public PrecomputeUtil getPrecomputeUtil() {
		return precomputeUtil;
	}
	public void setPrecomputeUtil(PrecomputeUtil precomputeUtil) {
		this.precomputeUtil = precomputeUtil;
	}
	public AllComponents(PhaseOne phaseOne,int window,int shift,double objects, int r){
		this.phaseOne = phaseOne;
		this.WINDOW_LENGTH = window;
		this.SHIFT_LENGTH = shift;
		this.NUMBER_OF_OBJECTS = objects;
		this.r = r;
	}
	
	
	
	
	public Map<String, Map<Integer, Map<String, Word>>> precomputeandCreateIndexMap(
			Map<String, Double[][]> normalizedMap) {
	
		Iterator<String> iterator = normalizedMap.keySet().iterator();
		double maxTf = 0;
		while (iterator.hasNext()) {
			String fileName = iterator.next();
			Double[][] normalizedArray = normalizedMap.get(fileName);
			for (int i = 0; i < normalizedArray.length; i++) {
				maxTf = 0;
				// StringBuilder str = new StringBuilder();
				Set<String> wordsSet = new HashSet<String>();
				List<String> str = new ArrayList<String>();
				for (int j = 0; j < normalizedArray[0].length; j++) {
					String band = phaseOne
							.getGaussianBand(normalizedArray[i][j]);
					str.add(band);
				}

				if (!precomputedMap.containsKey(fileName)) {
					precomputedMap.put(fileName,
							new HashMap<Integer, Map<String, Word>>());
				}

				if (!precomputedMap.get(fileName).containsKey(i)) {
					precomputedMap.get(fileName).put(i,
							new HashMap<String, Word>());
				}

				for (int k = 0; k < (str.size() - WINDOW_LENGTH); k = k
						+ SHIFT_LENGTH) {
					String wordName = "";
					for (int window = 0; window < WINDOW_LENGTH; window++) {
						wordName += str.get(k + window);
					}
					Map<String, Word> doc = precomputedMap.get(fileName).get(i);
					if (doc.containsKey(wordName)) {
						double newTf = doc.get(wordName).getTf() + 1;
						doc.get(wordName).setTf(newTf);
						doc.get(wordName).setRawTF(newTf);
						if (newTf > maxTf)
							maxTf = newTf;
					} else {
						Word word = new Word();
						word.setTf(1.0);
						word.setRawTF(1.0);
						word.setX(k);
						word.setY(i);
						doc.put(wordName, word);
						if(maxTf==0){
							maxTf = 1;
						}
					}
					if (!vocabulary.containsKey(wordName)) {
						vocabulary.put(wordName, 1.0);

					} else {
						if (!wordsSet.contains(wordName))
							vocabulary.put(wordName,
									vocabulary.get(wordName) + 1.0);
					}

					if (!dfMap.containsKey(wordName)) {
						dfMap.put(wordName, 1.0);

					} else {
						if (!wordsSet.contains(wordName))
							dfMap.put(wordName, dfMap.get(wordName) + 1.0);
					}

					if (!vocabulary2.containsKey(fileName)) {
						vocabulary2
								.put(fileName, new HashMap<String, Double>());
						vocabulary2.get(fileName).put(wordName, 1.0);
					} else {
						if (vocabulary2.get(fileName).containsKey(wordName)) {
							if (!wordsSet.contains(wordName))
								vocabulary2.get(fileName)
										.put(wordName,
												vocabulary2.get(fileName).get(
														wordName) + 1.0);
						} else
							vocabulary2.get(fileName).put(wordName, 1.0);
					}
					wordsSet.add(wordName);
				}

				// new code for tf here
				Map<String, Word> docMap = precomputedMap.get(fileName).get(i);
				for (String word : docMap.keySet()) {
					Word wordObj = docMap.get(word);
					wordObj.setRawTF(wordObj.getTf());
					double termfreq = wordObj.getTf() / maxTf;
					wordObj.setTf(termfreq);
					docMap.put(word, wordObj);
				}
				precomputedMap.get(fileName).get(i).putAll(docMap);
			}
		}// end while
		Iterator<String> lexiconIterator = vocabulary.keySet().iterator();
		while (lexiconIterator.hasNext()) {
			String wordName = lexiconIterator.next();
			double df = vocabulary.get(wordName);
			vocabulary.put(wordName, Math.log(NUMBER_OF_OBJECTS / df));

		}

		lexiconIterator = vocabulary2.keySet().iterator();
		while (lexiconIterator.hasNext()) {
			String docName = lexiconIterator.next();
			Iterator<String> docIterator = vocabulary2.get(docName).keySet()
					.iterator();
			while (docIterator.hasNext()) {
				String wordName = docIterator.next();
				double df = vocabulary2.get(docName).get(wordName);
				vocabulary2.get(docName).put(wordName, Math.log(20.0 / df));
			}

		}

		Iterator<String> gestureIterator = precomputedMap.keySet().iterator();
		while (gestureIterator.hasNext()) {
			String gestureName = gestureIterator.next();
			Iterator<Integer> sensorIterator = precomputedMap.get(gestureName)
					.keySet().iterator();
			while (sensorIterator.hasNext()) {
				int sensorName = sensorIterator.next();
				Iterator<String> wordIterator = precomputedMap.get(gestureName)
						.get(sensorName).keySet().iterator();
				while (wordIterator.hasNext()) {
					String wordName = wordIterator.next();
					Word word = precomputedMap.get(gestureName).get(sensorName)
							.get(wordName);
					word.setTFIdf(word.getTf() * vocabulary.get(wordName));
					word.setTFIdf2(word.getTf()
							* vocabulary2.get(gestureName).get(wordName));
				}
			}
		}
		return precomputedMap;
	}

	public void getQueryComputedMap(
			Map<String, Double[][]> normalizeQueryMap) {
		// Set<String> normalizedSet = normalizeQueryMap.keySet();
		Iterator<String> itr = normalizeQueryMap.keySet().iterator();
		while (itr.hasNext()) {
			String gesture = itr.next();
			Double[][] normalizedValues = normalizeQueryMap.get(gesture);
			double maxTf = 0;
			for (int i = 0; i < normalizedValues.length; i++) {
				maxTf = 0;
				Set<String> wordsSet = new HashSet<String>();
				List<String> str = new ArrayList<String>();
				for (int j = 0; j < normalizedValues[0].length; j++) {
					String band = phaseOne
							.getGaussianBand(normalizedValues[i][j]);
					str.add(band);
				}

				if (!queryMap.containsKey(gesture)) {
					queryMap.put(gesture,
							new HashMap<Integer, Map<String, Word>>());
				}
				if (!queryMap.get(gesture).containsKey(i)) {
					queryMap.get(gesture).put(i, new HashMap<String, Word>());
				}

				for (int k = 0; k < (str.size() - WINDOW_LENGTH); k = k
						+ SHIFT_LENGTH) {
					String wordName = "";
					for (int window = 0; window < WINDOW_LENGTH; window++) {
						wordName += str.get(k + window);
					}
					Map<String, Word> sensorMap = queryMap.get(gesture).get(i);
					if (sensorMap.containsKey(wordName)) {
						double newTf = sensorMap.get(wordName).getTf() + 1;
						sensorMap.get(wordName).setTf(newTf);
						sensorMap.get(wordName).setRawTF(newTf);
						if (newTf > maxTf)
							maxTf = newTf;
					} else {
						Word query = new Word();
						query.setTf(1);
						query.setRawTF(1);
						sensorMap.put(wordName, query);
						if(maxTf==0){
							maxTf = 1;
						}
					}

					if (!queryVocabulary1.containsKey(wordName)) {
						queryVocabulary1.put(wordName, 1.0);

					} else {
						if (!wordsSet.contains(wordName))
							queryVocabulary1.put(wordName,
									queryVocabulary1.get(wordName) + 1.0);
					}

					wordsSet.add(wordName);
				}

				Map<String, Word> docMap = queryMap.get(gesture).get(i);
				for (String word : docMap.keySet()) {
					Word wordObj = docMap.get(word);
					double termfreq = wordObj.getTf() / maxTf;
					wordObj.setTf(termfreq);
					docMap.put(word, wordObj);
				}
				queryMap.get(gesture).get(i).putAll(docMap);
			}
		} // end of while

		Iterator<String> docIterator = queryVocabulary1.keySet().iterator();
		while (docIterator.hasNext()) {
			String wordName = docIterator.next();
			double df = queryVocabulary1.get(wordName);
			queryVocabulary2.put(wordName, Math.log(20.0 / df));
		}

		Iterator<String> iterator = queryVocabulary1.keySet().iterator();
		while (iterator.hasNext()) {
			String wordName = iterator.next();
			//double df = queryVocabulary1.get(wordName);
			queryVocabulary1.put(
					wordName,
					Math.log((NUMBER_OF_OBJECTS/* +20.0 */)
							/ (dfMap.get(wordName) /*+df*/)));

			/*
			 * while(docIterator.hasNext()){ String wordName =
			 * docIterator.next(); double df =
			 * queryVocabulary1.get(docName).get(wordName);
			 * queryVocabulary2.get(docName).put(wordName,
			 * Math.log((NUMBER_OF_OBJECTS+20.0)/(dfMap.get(wordName)+df))); }
			 */
		}

		/*
		 * iterator= queryVocabulary1.keySet().iterator();
		 * while(iterator.hasNext()){ String docName = iterator.next();
		 */

		Iterator<String> gestureIterator = queryMap.keySet().iterator();
		while (gestureIterator.hasNext()) {
			String gestureName = gestureIterator.next();
			Iterator<Integer> sensorIterator = queryMap.get(gestureName)
					.keySet().iterator();
			while (sensorIterator.hasNext()) {
				int sensorName = sensorIterator.next();
				Iterator<String> wordIterator = queryMap.get(gestureName)
						.get(sensorName).keySet().iterator();
				while (wordIterator.hasNext()) {
					String wordName = wordIterator.next();
					Word query = queryMap.get(gestureName).get(sensorName)
							.get(wordName);
					query.setTFIdf(query.getTf()
							* queryVocabulary1.get(wordName));
					query.setTFIdf2(query.getTf()
							* queryVocabulary2.get(wordName));
				}
			}
		}
	}
	public Map<String, Map<Integer, Map<String, Word>>> getPrecomputedMap() {
		return precomputedMap;
	}
	public void setPrecomputedMap(
			Map<String, Map<Integer, Map<String, Word>>> precomputedMap) {
		this.precomputedMap = precomputedMap;
	}
	public  Map<String, Map<Integer, Map<String, Word>>> getQueryMap() {
		return queryMap;
	}
	public void setQueryMap(
			Map<String, Map<Integer, Map<String, Word>>> queryMap) {
		this.queryMap = queryMap;
	}

	
}
