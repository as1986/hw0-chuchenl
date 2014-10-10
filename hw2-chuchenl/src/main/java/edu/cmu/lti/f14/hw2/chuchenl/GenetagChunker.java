package edu.cmu.lti.f14.hw2.chuchenl;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.aliasi.chunk.Chunk;
import com.aliasi.util.AbstractExternalizable;

/**
 * Lingpipe GenetagChunker wrapper
 * 
 * @author Chu-Cheng Lin
 * 
 */
public class GenetagChunker {
  private static GenetagChunker inst = null;

  private static String filePath = null;

  com.aliasi.chunk.ConfidenceChunker ch;

  /**
   * private constructor. Loads NE chunker model
   * 
   * @param path
   *          location of the model
   * @throws ClassNotFoundException
   * @throws IOException
   */
  private GenetagChunker(String path) throws ClassNotFoundException, IOException {
    File modelFile = new File(path);
    ch = (com.aliasi.chunk.ConfidenceChunker) AbstractExternalizable.readObject(modelFile);
    return;
  }

  /**
   * Sets model path.
   * @param s model path
   */
  public static void setFilePath(String s) {
    filePath = s;
  }

  /**
   * if the singleton exists, return it; otherwise create a new one
   * @return GenetagChunker instance
   * @throws ClassNotFoundException
   * @throws IOException
   */
  public static GenetagChunker getInstance() throws ClassNotFoundException, IOException {
    if (inst == null) {
      inst = new GenetagChunker(filePath);
    }
    return inst;
  }

  /**
   * returns NE chunks from Lingpipe
   * 
   * @param sen
   *          text to chunk
   * @return set of Chunks
   * @see com.aliasi.chunk.Chunk
   */
  public Set<Chunk> chunk(String sen) {
    Set<Chunk> toReturn = new HashSet<Chunk>();
    char[] senChArray = sen.toCharArray();
    Iterator<Chunk> it = ch.nBestChunks(senChArray, 0, senChArray.length, 10);
    while (it.hasNext()) {
      toReturn.add(it.next());
    }
    return toReturn;
  }
}
