package edu.cmu.lti.f14.hw2.chuchenl;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.TokenShapeChunker;
import com.aliasi.util.AbstractExternalizable;

public class GeniaChunker {
  private static GeniaChunker inst = null;

  private static String filePath = null;

  private TokenShapeChunker chunker;

  /**
   * private constructor. Loads NE chunker model
   * 
   * @param path
   *          location of the model
   * @throws ClassNotFoundException
   * @throws IOException
   */
  private GeniaChunker(String path) throws ClassNotFoundException, IOException {
    File modelFile = new File(filePath);
    chunker = (TokenShapeChunker) AbstractExternalizable.readObject(modelFile);
    return;
  }

  /**
   * Sets model path.
   * 
   * @param s
   *          model path
   */
  public static void setFilePath(String s) {
    filePath = s;
  }

  /**
   * if the singleton exists, return it; otherwise create a new one
   * 
   * @return GenetagChunker instance
   * @throws ClassNotFoundException
   * @throws IOException
   */
  public static GeniaChunker getInstance() throws ClassNotFoundException, IOException {
    if (inst == null) {
      inst = new GeniaChunker(filePath);
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
    return chunker.chunk(sen).chunkSet();
  }

}
