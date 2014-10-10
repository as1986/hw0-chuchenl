package edu.cmu.lti.f14.hw2.chuchenl;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import com.aliasi.chunk.Chunk;

public class GeniaAnnotator extends JCasAnnotator_ImplBase {
  GeniaChunker cg;

  int processed = 0;

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    try {
      String tokenShapePath = (String) context.getConfigParameterValue("TokenShapePath");
      GeniaChunker.setFilePath(tokenShapePath);
      cg = GeniaChunker.getInstance();
    } catch (ClassNotFoundException e) {
      throw new ResourceInitializationException(e);
    } catch (IOException e) {
      throw new ResourceInitializationException(e);
    }
  }

  @Override
  public void process(JCas doc) throws AnalysisEngineProcessException {
    AnnotationIndex<org.apache.uima.jcas.tcas.Annotation> splitted = doc
            .getAnnotationIndex(Sentence.type);
    Iterator<org.apache.uima.jcas.tcas.Annotation> it = splitted.iterator();
    while (it.hasNext()) {
      Sentence sent = (Sentence) it.next();
      String text = sent.getText().trim();
      // Vector<String[][]> g = abner.getChunks(text);
      // String[][] ss = g.get(0);

      Set<Chunk> chunked = cg.chunk(text);
      processed += chunked.size();
      for (Chunk c : chunked) {
        GeneAnnotation ann = new GeneAnnotation(doc);
        String covered = text.substring(c.start(), c.end());
        int whiteBeforeStart = c.start() - text.substring(0, c.start()).replace(" ", "").length();
        int whiteBeforeEnd = c.end() - text.substring(0, c.end()).replace(" ", "").length();
        ann.setBegin(c.start() - whiteBeforeStart);
        ann.setEnd(c.end() - whiteBeforeEnd - 1);

        ann.setContent(covered);
        ann.setId(sent.getId());
        ann.setConfidence(c.score());
        ann.setSource("GENIA");
        ann.addToIndexes();
      }

    }

  }

}
