package edu.cmu.lti.f14.hw2.chuchenl;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;

public class MyConsumer extends CasConsumer_ImplBase {

  /**
   * parameter configuration
   * 
   * two properties: w_G and \theta.
   * 
   * @author Chu-Cheng Lin
   * 
   */
  class Pair {
    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + getOuterType().hashCode();
      result = prime * result + ((genia == null) ? 0 : genia.hashCode());
      result = prime * result + ((threshold == null) ? 0 : threshold.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      Pair other = (Pair) obj;
      if (!getOuterType().equals(other.getOuterType()))
        return false;
      if (genia == null) {
        if (other.genia != null)
          return false;
      } else if (!genia.equals(other.genia))
        return false;
      if (threshold == null) {
        if (other.threshold != null)
          return false;
      } else if (!threshold.equals(other.threshold))
        return false;
      return true;
    }

    Pair(Float g, Float t) {
      genia = g;
      threshold = t;
    }

    Float genia;

    Float threshold;

    private MyConsumer getOuterType() {
      return MyConsumer.this;
    }
  }

  private StringBuilder sb;

  /**
   * output filenames during search
   */
  private Map<Pair, String> parameterSearch = new HashMap<Pair, String>();

  /**
   * PrintWriters during search
   */
  private Map<Pair, PrintWriter> parameterSearchWriter = new HashMap<Pair, PrintWriter>();

  List<Float> geniaWeights = new ArrayList<Float>();

  List<Float> thresholds = new ArrayList<Float>();

  private Float geniaLower, geniaUpper, thresLower, thresUpper;

  private boolean search;

  private String outputPath;

  /**
   * loads parameters from the configuration file. Also initializes Lists geniaWeights and
   * thresholds.
   * 
   * If not in search mode, put the fixed parameters into the search range. That is, we still
   * `search', but the range consists only of a single configuration.
   */
  private void loadParams() {
    search = (Boolean) getConfigParameterValue("Search");
    outputPath = (String) getConfigParameterValue("OutputPath");
    if (search) {
      geniaLower = (Float) getConfigParameterValue("GeniaLower");
      geniaUpper = (Float) getConfigParameterValue("GeniaUpper");
      thresLower = (Float) getConfigParameterValue("ThresLower");
      thresUpper = (Float) getConfigParameterValue("ThresUpper");
      // initialize value lists
      for (Float i = geniaLower; i < geniaUpper; i += 0.1f) {
        geniaWeights.add(i);
      }
      for (Float i = thresLower; i < thresUpper; i += 0.1f) {
        thresholds.add(i);
      }
    } else {
      geniaWeights.add((Float) getConfigParameterValue("Genia"));
      thresholds.add((Float) getConfigParameterValue("Thres"));
    }
  }

  /**
   * initialization. Creates PrintWriters here.
   */
  @Override
  public void initialize() throws ResourceInitializationException {
    super.initialize();
    loadParams();
    sb = new StringBuilder();
    try {

      for (Float g : geniaWeights) {
        for (Float t : thresholds) {
          String path;
          if (search) {
            path = outputPath + "_search/param_" + g + "_" + t;
          } else {
            path = outputPath;
          }

          Pair p = new Pair(g, t);
          parameterSearch.put(p, path);
          parameterSearchWriter.put(p, new PrintWriter(path, "UTF-8"));
        }
      }

    } catch (FileNotFoundException e) {
      throw new ResourceInitializationException(e);
    } catch (UnsupportedEncodingException e) {
      throw new ResourceInitializationException(e);
    }
  }

  @Override
  public void processCas(CAS c) throws ResourceProcessException {
    Map<Pair, Map<String, Float>> allEntities = new HashMap<Pair, Map<String, Float>>();
    Map<Pair, Set<String>> genetagPresentSets = new HashMap<Pair, Set<String>>();

    for (Float g : geniaWeights) {
      for (Float t : thresholds) {
        Pair p = new Pair(g, t);
        allEntities.put(p, new HashMap<String, Float>());
        genetagPresentSets.put(p, new HashSet<String>());
      }
    }

    JCas jcas;
    try {
      jcas = c.getJCas();
    } catch (CASException e) {
      throw new ResourceProcessException(e);
    }
    FSIterator<?> it = jcas.getAnnotationIndex(GeneAnnotation.type).iterator();
    Map<String, Float> entities;
    while (it.hasNext()) {
      GeneAnnotation n = (GeneAnnotation) it.next();
      sb.append(n.getId()).append("|").append(n.getBegin()).append(" ").append(n.getEnd())
              .append("|").append(n.getContent());
      String lineOutput = sb.toString();

      for (Pair p : allEntities.keySet()) {
        entities = allEntities.get(p);
        if (n.getSource() == "GENETAG") {
          Set<String> genetagPresentSet = genetagPresentSets.get(p);
          genetagPresentSet.add(lineOutput);
        }
        Float score = computeScore(n, p.genia);
        Float prev_score = 0.f;
        if (entities.containsKey(lineOutput)) {
          prev_score = entities.get(lineOutput);
        }
        entities.put(lineOutput, score + prev_score);
      }

      sb.setLength(0);
    }
    for (Float g : geniaWeights) {
      for (Float t : thresholds) {
        Pair p = new Pair(g, t);
        entities = allEntities.get(p);
        for (String k : entities.keySet()) {
          // only keep terms that are found by the GENETAG chunker
          if (!genetagPresentSets.get(p).contains(k)) {
            continue;
          }
          Float confidence = entities.get(k);
          // only keep terms with high reweighed score
          if (confidence > t) {
            parameterSearchWriter.get(p).println(k);
          }
        }
      }
    }

  }

  /**
   * computes reweighed score with g
   * 
   * @param n
   *          gene annotation
   * @param g
   *          w_G
   * @return reweighed score
   */
  private Float computeScore(GeneAnnotation n, Float g) {
    if (n.getSource() == "GENETAG") {
      return ((Double) n.getConfidence()).floatValue();
    } else if (n.getSource() == "GENIA") {
      return g;
    } else { // unsupported
      throw new UnsupportedOperationException("Only supports GENIA and GENETAG for now");
    }

  }

}
