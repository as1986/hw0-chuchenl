package edu.cmu.lti.f14.hw2.chuchenl;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunking;
import com.aliasi.util.AbstractExternalizable;

/**
 * Lingpipe Chunker wrapper
 * 
 * @author Chu-Cheng Lin
 * 
 */
public class Chunker {
  File modelFile;

  com.aliasi.chunk.Chunker ch;

  /**
   * constructor. Loads NE chunker model
   * 
   * @param path
   *          location of the model
   * @throws ClassNotFoundException
   * @throws IOException
   */
  public Chunker(String path) throws ClassNotFoundException, IOException {
    modelFile = new File(path);
    ch = (com.aliasi.chunk.Chunker) AbstractExternalizable.readObject(modelFile);
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
    Set<Chunk> toReturn;
    Chunking chunked = ch.chunk(sen);
    toReturn = chunked.chunkSet();
    return toReturn;
  }
}
