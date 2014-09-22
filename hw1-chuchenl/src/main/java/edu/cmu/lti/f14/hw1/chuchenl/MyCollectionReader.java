package edu.cmu.lti.f14.hw1.chuchenl;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;

public class MyCollectionReader extends CollectionReader_ImplBase {
  List<String> lines;

  int current;

  Iterator<String> currentLine;

  @Override
  public void getNext(CAS aCAS) throws IOException, CollectionException {
    JCas jcas;
    try {
      jcas = aCAS.getJCas();
      String s = currentLine.next();
      current++;
      jcas.setDocumentText(s);

      // id, text
      String[] splitted = s.split(" ", 2);
      if (splitted.length > 1) {
        Sentence sent = new Sentence(jcas);
        sent.setText(splitted[1]);
        sent.setId(splitted[0]);
        sent.addToIndexes();
      }

    } catch (CASException e) {
      throw new CollectionException(e);
    }

  }

  @Override
  public Progress[] getProgress() {
    return new Progress[] { new ProgressImpl(current, lines.size(), Progress.ENTITIES) };
  }

  @Override
  public boolean hasNext() throws IOException, CollectionException {
    if (currentLine.hasNext()) {
      return true;
    }
    return false;
  }

  @Override
  public void initialize() throws ResourceInitializationException {
    super.initialize();
    String PATH = (String) getConfigParameterValue("InputFileName");
    try {
      lines = Files.readAllLines(Paths.get(PATH), Charset.defaultCharset());
    } catch (IOException e) {
      throw new ResourceInitializationException(e);
    }
    current = 0;
    currentLine = lines.iterator();
  }

  /**
   * 
   */
  @Override
  public void close() throws IOException {
    return;
  }

}
