package edu.cmu.lti.f14.hw1.chuchenl;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;

public class MyConsumer extends CasConsumer_ImplBase {

  private PrintWriter outputWriter;

  private StringBuilder sb;

  @Override
  public void initialize() throws ResourceInitializationException {
    super.initialize();
    sb = new StringBuilder();
    try {
      String outputPath = (String) getConfigParameterValue("OutputPath");
      outputWriter = new PrintWriter(outputPath, "UTF-8");
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (UnsupportedEncodingException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Override
  public void processCas(CAS c) throws ResourceProcessException {
    // TODO Auto-generated method stub
    JCas jcas;
    try {
      jcas = c.getJCas();
    } catch (CASException e) {
      throw new ResourceProcessException(e);
    }
    // outputWriter.println(jcas.getDocumentText());
    FSIterator it = jcas.getAnnotationIndex(GeneAnnotation.type).iterator();
    if (it.hasNext()) {
      GeneAnnotation n = (GeneAnnotation) it.next();

      sb.append(n.getId()).append("|").append(n.getBegin()).append(" ").append(n.getEnd())
              .append("|").append(n.getContent());
      String lineOutput = sb.toString();
      sb.setLength(0);
      outputWriter.println(lineOutput);
    }

  }
}
